package com.hwilliamgo.fuzzy_video_drill.camera

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description: 摄像机工厂类
 */
object CameraFactory {
    fun createCamera(cameraType: CameraImplType): ICamera {
        return when (cameraType) {
            CameraImplType.CAMERA_1 -> SimpleCamera()
            CameraImplType.CAMERA_2 -> SimpleCamera()
            CameraImplType.CAMERA_X -> SimpleCamera()
        }
    }
}