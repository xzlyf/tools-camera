# 此库基于Camera X开发

## 硬性条件

- API >= 21

# 什么是Camera X

- 在 Android 应用中要实现 Camera
  功能还是比较困难的，为了保证在各品牌手机设备上的兼容性、响应速度等体验细节，Camera
  应用的开发者往往需要花很大的时间和精力进行测试，甚至需要手动在数百种不同设备上进行测试。CameraX
  正是为解决这个痛点而诞生的。

- CameraX 的优势

- CameraX 和 Lifecycle 结合在一起，方便开发者管理生命周期。且相比较 camera2
  减少了大量样板代码的使用。

- CameraX 是基于 Camera2 API 实现的，兼容至 Android L (API
  21)，从而确保兼容到市面上绝大多数手机

- 开发者可以通过扩展的形式使用和原生摄像头应用同样的功能（如：人像、夜间模式、HDR、滤镜、美颜）

- Google 自己还打造了 CameraX
  自动化测试实验室，对摄像头功能进行深度测试，确保能覆盖到更加广泛的设备。相当于 Google
  帮我们把设备兼容测试工作给做了。
