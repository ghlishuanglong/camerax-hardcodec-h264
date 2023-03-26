#include <jni.h>
#include <malloc.h>
#include <cstring>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("h264");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("h264")
//      }
//    }
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_camerax_hardcodec_h264_util_YUVUtils_00024Companion_yuvToNV21(JNIEnv *env, jobject thiz, jint width,
                                                                       jint height, jobject byte_buffer_y,
                                                                       jint byte_buffer_y_length,
                                                                       jobject byte_buffer_u,
                                                                       jint byte_buffer_u_length,
                                                                       jobject byte_buffer_v,
                                                                       jint byte_buffer_v_length) {

    auto *y_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_y);
    auto *u_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_u);
    auto *v_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_v);
    if (y_buffer != nullptr && u_buffer != nullptr && v_buffer != nullptr) {

        auto *nv21Array = static_cast< jbyte *>(malloc(sizeof(jbyte) * width * height * 3 / 2));

        memcpy(nv21Array, y_buffer, byte_buffer_y_length);
        memcpy(nv21Array + byte_buffer_y_length, v_buffer, byte_buffer_v_length);
        nv21Array[byte_buffer_y_length + byte_buffer_v_length]  = u_buffer[byte_buffer_u_length - 1];

        jbyteArray nv21Data = env->NewByteArray(width * height * 3 / 2);
        env->SetByteArrayRegion(nv21Data, 0, width * height * 3 / 2, nv21Array);

        free(nv21Array);
        nv21Array = nullptr;

        return nv21Data;
    }
    return nullptr;
}