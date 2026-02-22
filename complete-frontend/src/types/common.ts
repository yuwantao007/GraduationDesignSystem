/**
 * 通用类型定义
 */

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

// 选项类型
export interface Option {
  label: string
  value: string | number
  disabled?: boolean
  children?: Option[]
}

// 菜单类型
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
  permission?: string
}

// 面包屑类型
export interface BreadcrumbItem {
  title: string
  path?: string
}

// 表格列配置
export interface TableColumn {
  title: string
  dataIndex: string
  key: string
  width?: number
  align?: 'left' | 'center' | 'right'
  fixed?: 'left' | 'right'
  sorter?: boolean
  customRender?: (params: { text: any; record: any; index: number }) => any
}
