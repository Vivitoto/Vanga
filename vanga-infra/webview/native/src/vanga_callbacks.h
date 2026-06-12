#ifndef VANGA_CALLBACKS_H
#define VANGA_CALLBACKS_H

#include <jni.h>
#include <webview/webview.h>

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    jstring name;
    jobject object;
    jmethodID method;
    char *name_chars;
} bind_callback_t;

typedef struct {
    JavaVM *jvm;
    jobject object;
    jmethodID method;
} main_started_callback_t;

typedef struct {
    webview_t webview;
    JavaVM *jvm;
    jobject object;
    jmethodID method;
} request_interceptor;

typedef struct {
    void *data;
    long size;
    char *content_type;
} load_result_t;

void vanga_main_started_callback(webview_t webview, /*main_started_callback_t*/ void *data);

load_result_t *vanga_interceptor_run(request_interceptor *interceptor, const char *uri);

request_interceptor *vanga_interceptor_create(JNIEnv *env, jobject jvm_interceptor, webview_t webview);

void vanga_interceptor_destroy(/*resource_loader_t*/ void *data);

void vanga_bind_callback_run(const char *id, const char *req, /*bind_callback_t*/ void *data);

bind_callback_t *vanga_bind_callback_create(JNIEnv *env, jstring name, jobject jvm_callback, webview_t webview);

void vanga_bind_callback_destroy(/*bind_callback_t*/ void *data);

void vanga_webview_bind_dispatch(webview_t webview, void *data);

#endif //VANGA_CALLBACKS_H
