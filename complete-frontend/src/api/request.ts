/**
 * Axios HTTP 请求封装
 * 统一处理请求拦截、响应拦截、错误处理
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'

// 响应数据结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // Blob 类型响应（文件下载）直接放行，不做业务 code 判断
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data

    // 根据业务状态码判断
    if (res.code === 200) {
      return response
    } else if (res.code === 401) {
      // Token 失效，清除登录信息并跳转登录页
      message.error('登录已过期，请重新登录')
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      window.location.href = '/login'
      return Promise.reject(new Error(res.message || '登录已过期'))
    } else {
      // 其他错误提示
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          message.error(data.message || '请求参数错误')
          break
        case 401:
          message.error('未授权，请重新登录')
          localStorage.removeItem('access_token')
          localStorage.removeItem('refresh_token')
          window.location.href = '/login'
          break
        case 403:
          message.error('拒绝访问')
          break
        case 404:
          message.error('请求资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(data.message || '请求失败')
      }
    } else if (error.request) {
      message.error('网络错误，请检查网络连接')
    } else {
      message.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
class HttpRequest {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.get(url, config).then(res => res.data)
  }

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.post(url, data, config).then(res => res.data)
  }

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.put(url, data, config).then(res => res.data)
  }

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.delete(url, config).then(res => res.data)
  }

  upload<T = any>(url: string, formData: FormData, onUploadProgress?: (progressEvent: any) => void): Promise<ApiResponse<T>> {
    return service.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    }).then(res => res.data)
  }

  download(url: string, filename: string, config?: AxiosRequestConfig): Promise<void> {
    return service
      .get(url, {
        ...config,
        responseType: 'blob'
      })
      .then((response: any) => {
        const blob = new Blob([response.data])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = filename
        link.click()
        URL.revokeObjectURL(link.href)
      })
  }
}

export default new HttpRequest()
