#include <jni.h>

JNIEXPORT jint JNICALL Java_JNI_1example_magic
  (JNIEnv *, jclass);

jint JNICALL
Java_JNI_1example_magic(
    JNIEnv *env,
    jclass jcl)
{
    return 12345;
}
