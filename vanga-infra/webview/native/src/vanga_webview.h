#ifndef VANGA_WEBVIEW_H
#define VANGA_WEBVIEW_H

#include <jni.h>
#include "vanga_callbacks.h"
#include <glib.h>

typedef void *vanga_webview_t;

static inline void vanga_throw_jvm_exception(JNIEnv *env, const char *message) {
    jclass class = (*env)->FindClass(env, "snd/webview/WebviewException");
    (*env)->ThrowNew(env, class, message);
}

vanga_webview_t vanga_webview_create(JNIEnv *env, jobject awt_window);

void vanga_webview_destroy(vanga_webview_t data);

webview_t vanga_webview_get_webview(vanga_webview_t);

JavaVM *vanga_webview_get_jvm(vanga_webview_t);

void vanga_webview_bind(vanga_webview_t webview, bind_callback_t *callback);

void vanga_register_request_interceptor(vanga_webview_t webview, request_interceptor *interceptor);


#endif //VANGA_WEBVIEW_H
