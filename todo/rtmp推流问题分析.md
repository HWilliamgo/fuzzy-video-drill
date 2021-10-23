### 栈溢出导致的崩溃：
1. RTMP_SendPacket
2. WriteN
3. RTMP_Close
4. SendFCUnpublish
5. RTMP_SendPacket

然后陷入无限循环，最终导致栈溢出崩溃。

现在有两个问题：

1.为什么发送发送着就不能发送了？是因为数据格式不正确，远端关闭了连接导致的吗？
todo: 通过在斗鱼平台做直播，发现应该是码流格式不正确导致的。
ret : 将h264码流文件输出后，发现是可以播放的，那么应该是发出去的rtmp包出了问题。可能是rtmp包里面没有关键帧导致的。

2.如何解决这个不断重发导致的崩溃问题？


用斗鱼直播测试：
1.发了852个frame都没有崩溃，说明斗鱼的rtmp接收端不会对码流数据做校验，不会主动断开连接。
2. 但是在点击返回退出直播的时候，发生了这样的崩溃：
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG: SYSVMTYPE: Art
    APPVMTYPE: Art
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG: pid: 21402, tid: 21402, name: zzy_video_drill  >>> com.hwilliamgo.fuzzy_video_drill <<<
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG: signal 11 (SIGSEGV), code 2 (SEGV_ACCERR), fault addr 0x7a04c00000
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x0  0000007a04c00000  x1  0000007ff5be9a20  x2  0000007a8a973212  x3  0000000000000010
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x4  0000000000000000  x5  000000007fffffff  x6  0000000000000002  x7  0000000000000030
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x8  0101010101010101  x9  0000007a8a936177  x10 0000000000000002  x11 0000007ff5be960d
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x12 0000007ff5be9ba8  x13 0000000000000030  x14 0000000000000000  x15 0000007ff5be9b98
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x16 0000007a8a9710b8  x17 0000007a8a8a0890  x18 0000000000000001  x19 0000007ff5be9bb0
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x20 000000008000002f  x21 0000007a8a973212  x22 0000000000000030  x23 0000007ff5be9b90
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x24 0000000000000000  x25 0000007a8b59f7e0  x26 000000000ccccccc  x27 0000007ff5be9ba8
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     x28 00000079faddc34b  x29 0000007ff5be9b80
2021-10-16 17:16:14.099 22163-22163/? A/DEBUG:     sp  0000007ff5be9400  lr  0000007a8a8e303c  pc  0000007a8a8a08a0
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG: backtrace:
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #00 pc 000000000001e8a0  /system/lib64/libc.so (strlen+16)//
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #01 pc 0000000000061038  /system/lib64/libc.so (__vfprintf+6004)
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #02 pc 000000000006e6f4  /system/lib64/libc.so (vasprintf+152)
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #03 pc 000000000002f4ec  /data/app/com.hwilliamgo.fuzzy_video_drill-yCEdpsraxVzCLxZoaF8pUA==/base.apk (offset 0x130b000) (MyLog::dTag(char const*, char const*, ...)+228)
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #04 pc 000000000001bf48  /data/app/com.hwilliamgo.fuzzy_video_drill-yCEdpsraxVzCLxZoaF8pUA==/base.apk (offset 0x130b000)
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #05 pc 0000000000019a4c  /data/app/com.hwilliamgo.fuzzy_video_drill-yCEdpsraxVzCLxZoaF8pUA==/base.apk (offset 0x130b000) (VideoEncoder::logHook(void*, int, char const*, std::__va_list)+140)
2021-10-16 17:16:14.103 22163-22163/? A/DEBUG:     #06 pc 0000000000060830  /data/app/com.hwilliamgo.fuzzy_video_drill-yCEdpsraxVzCLxZoaF8pUA==/base.apk (offset 0x130b000)