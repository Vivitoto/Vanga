#ifndef VANGA_VIPS_COMMON_JNI_H
#define VANGA_VIPS_COMMON_JNI_H

#include <jni.h>
#include <vips/vips.h>

JNIEXPORT void vanga_throw_jvm_vips_exception_message(JNIEnv *env, const char *message);

JNIEXPORT void vanga_throw_jvm_vips_exception(JNIEnv *env);

JNIEXPORT jobject vanga_to_jvm_image_data(JNIEnv *env, VipsImage *decoded);

JNIEXPORT VipsImage *vanga_from_jvm_handle(JNIEnv *env, jobject jvm_image);

JNIEXPORT jobject vanga_to_jvm_handle(JNIEnv *env,
                                       VipsImage *image,
                                       const unsigned char *external_source_buffer);

#endif // VANGA_VIPS_COMMON_JNI_H