# ErrorLog — Mapper 与 Service 重构优化策略

- 日期：2026-02-20
- 作者：重构执行者

## 一、概要

将单表 CRUD 优先使用 MyBatis-Plus 的 `BaseMapper` + `QueryWrapper` / `LambdaQueryWrapper`，
仅对确实需要数据库函数或多表 JOIN 的场景保留手写 SQL。目标为统一代码风格、降低重复 SQL、便于维护与自动化填充。

## 二、变更要点

- **UserMapper**
  - 移除：`selectByUsername()`、`updateUserStatus()`（这类单表查询/更新使用 QueryWrapper 或 `updateById` 替代）
  - 保留：`updateLoginInfo()`（使用数据库 `NOW()` 函数，保证原子性与时间一致性）

- **RoleMapper**
  - 移除：`selectByRoleCode()`（单表查询改用 `LambdaQueryWrapper`）
  - 保留：`selectRolesByUserId()`（多表 JOIN，保留手写 SQL）

- **UserRoleMapper**
  - 移除：`deleteByUserId()`（改为 `userRoleMapper.delete(new LambdaQueryWrapper<>() .eq(...))`）

- **RolePermissionMapper**
  - 移除：`deleteByRoleId()`（同上，使用 QueryWrapper.delete）

- **PermissionMapper**
  - 保留所有现有手写 SQL（均为多表 JOIN 查询，属于复杂查询场景）

## 三、Service 层调整（摘要）

- `AuthServiceImpl`：将 `selectByUsername` / `selectByRoleCode` 改为使用 `LambdaQueryWrapper` 的 `selectOne`。
- `UserServiceImpl`：将 `selectByUsername` 改为 `selectOne(LambdaQueryWrapper)`；对状态更新改为 `selectById` + `updateById`（以触发自动填充）；删除用户角色关系改为 `delete(new LambdaQueryWrapper<>()...)`。
- `SecurityUserDetailsService`：将 `selectByUsername` 改为 `LambdaQueryWrapper` 查询。

## 四、变更理由

- 统一单表操作到 MyBatis-Plus 能减少手写 SQL、利用框架自动填充、提高可读性与一致性。
- 仅在多表 JOIN 或需数据库函数（如 `NOW()`）时保留手写 SQL，保证性能与原子性需求。

## 五、风险与回退策略

- 若重构导致编译、集成或运行失败，可通过版本控制回退对应 Mapper 文件到手写 SQL 实现，或临时在 Service 中恢复原有调用以保证线上稳定。

## 六、后续建议

- 执行 `mvn compile` 和现有测试套件，确认无回归。若有 CI/CD，请在 PR 中加入变更说明与影响范围。
- 若团队同意此规范，可将其写入项目贡献指南或 Coding Style 文档，作为日后统一标准。