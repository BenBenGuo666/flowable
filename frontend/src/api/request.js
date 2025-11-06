import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  // 开发环境使用相对路径，通过 Vite 代理转发到后端
  // 生产环境可通过环境变量配置
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 可以在这里添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data } = response

    // 如果返回的状态码为200，说明接口请求成功
    if (response.status === 200) {
      return data
    }

    // 其他情况提示错误
    console.error('响应错误:', data.message || '未知错误')
    return Promise.reject(new Error(data.message || '未知错误'))
  },
  error => {
    console.error('响应错误:', error)

    // 处理常见错误
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，跳转登录页
          console.error('未授权，请重新登录')
          break
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`连接错误: ${error.response.status}`)
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
    }

    return Promise.reject(error)
  }
)

export default request
