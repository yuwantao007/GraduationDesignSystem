# Excel批量导入功能 - 问题记录与解决方案

> 功能背景：为专业管理页面添加通过Excel表批量导入专业方向、专业及教师的功能。

---

## 问题一：Vue 3 中 v-model 绑定 prop 报错

### 问题描述
`ImportMajorModal.vue` 中使用 `v-model:open="open"`，而 `open` 本身是从父组件传入的 prop。
在 Vue 3 中，prop 是只读的，直接对 prop 进行 v-model 绑定会导致运行时警告和功能异常。

### 错误表现
```
[Vue warn]: Mutating a prop directly since it is not allowed...
```

### 解决方案
将组件内部对 `open` prop 的直接修改改为通过 `emit` 通知父组件：

```vue
<!-- 错误写法 -->
<script setup>
const props = defineProps<{ open: boolean }>()
// 直接修改 props.open 是不允许的
</script>

<!-- 正确写法 -->
<script setup>
const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', value: boolean): void }>()

// 关闭时通过 emit 通知父组件
const handleClose = () => emit('update:open', false)
</script>
```

---

## 问题二：commons-compress 版本冲突导致 NoSuchMethodError

### 问题描述
后端下载 Excel 模板时抛出：
```
java.lang.NoSuchMethodError: 'void org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.putArchiveEntry(ZipArchiveEntry)'
```

### 根本原因
Apache POI（用于生成 `.xlsx` 文件）内部依赖 `commons-compress`。
较新版本的 `commons-compress` 将 `putArchiveEntry(ZipArchiveEntry)` 方法签名改为 `putArchiveEntry(ArchiveEntry)`，导致 POI 调用时找不到原方法。

### 排查方式
```bash
mvn dependency:tree "-Dincludes=org.apache.commons:commons-compress"
```
查看实际解析到的 commons-compress 版本，判断是否与 POI 所需版本不兼容。

### 解决方案
在 `pom.xml` 中显式锁定 commons-compress 版本为 POI 5.x 兼容版本（`1.21`）：

```xml
<!-- 方案一：直接添加依赖覆盖传递版本 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-compress</artifactId>
    <version>1.21</version>
</dependency>

<!-- 方案二：通过 dependencyManagement 强制指定版本 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
            <version>1.21</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> 修改 pom.xml 后必须重新执行 `mvn clean install` 并重启后端服务才能生效。

### 备选方案
若版本锁定仍有问题，可考虑将 Excel 库替换为阿里巴巴的 **EasyExcel**，其与 Spring Boot 集成更友好，且不依赖 commons-compress：
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.3.2</version>
</dependency>
```

---

## 问题三：模板下载静默失败（无报错但显示"模板下载失败"）

### 问题描述
前后端均无报错日志，但前端仍弹出"请求失败，模板下载失败"提示。

### 可能原因排查列表

| 可能原因 | 排查方式 |
|---------|---------|
| pom.xml 修改未生效（未重启服务） | 确认已执行 `mvn clean install` 并重启后端 |
| 前后端接口路径不一致 | 对比 `MajorController` 中的 `@RequestMapping` 路径与前端 `api/major.ts` 中的请求 URL |
| 接口需要权限认证但请求未携带 Token | 检查该接口是否有 `@PreAuthorize` 注解，以及前端请求头是否携带 `Authorization` |
| 前端 Blob 下载错误处理吞掉了真实错误 | 当响应为非 200 时，若前端将 error response 当作 Blob 处理，会静默失败 |
| 响应 Content-Type 设置不正确 | 后端需设置 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |

### 前端 Blob 下载正确错误处理示例
```typescript
const downloadTemplate = async () => {
  try {
    const response = await axios.get('/major/import/template', {
      responseType: 'blob'
    })
    // 检查是否为错误响应（JSON 格式）
    if (response.data.type === 'application/json') {
      const text = await response.data.text()
      const error = JSON.parse(text)
      message.error(error.message || '模板下载失败')
      return
    }
    // 正常下载
    const url = URL.createObjectURL(response.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '专业导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    message.error('模板下载失败')
    console.error(e)
  }
}
```

### 后端模板下载接口标准写法
```java
@GetMapping("/import/template")
public void downloadImportTemplate(HttpServletResponse response) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=major_import_template.xlsx");
    response.setCharacterEncoding("UTF-8");
    try (XSSFWorkbook workbook = majorService.buildImportTemplate();
         OutputStream os = response.getOutputStream()) {
        workbook.write(os);
        os.flush();
    } catch (Exception e) {
        log.error("下载模板失败", e);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":500,\"message\":\"模板下载失败\"}");
    }
}
```

---

## 涉及文件清单

| 文件 | 修改内容 |
|------|---------|
| `complete-backend/pom.xml` | 锁定 commons-compress 版本为 1.21 |
| `complete-backend/.../controller/MajorController.java` | 添加 `/major/import/template` 和 `/major/import` 接口 |
| `complete-backend/.../service/impl/MajorServiceImpl.java` | 实现 `downloadImportTemplate()` 和 `importMajors()` 方法 |
| `complete-frontend/src/components/major/ImportMajorModal.vue` | 新建组件，修复 v-model prop 错误 |
| `complete-frontend/src/api/major.ts` | 添加 `downloadImportTemplate()` 和 `importMajors()` API 函数 |
| `complete-frontend/src/views/.../MajorManagement.vue` | 添加"Excel 导入"按钮并引入 `ImportMajorModal` |

---

## 待办事项

- [ ] 确认 commons-compress 版本冲突已彻底解决（重启后端验证）
- [ ] 端到端测试：下载模板 → 填写 → 上传 → 导入
- [ ] 修复专业详情页教师列表显示问题
- [ ] 修复企业概览中教师数量统计问题
- [ ] 完成 `EnterpriseFormModal.vue` 的遗留功能
