#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include <android/log.h>

#define LOG_TAG "EdgeVisionNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_edgevision_NativeProcessor_processFrame(
        JNIEnv *env,
        jobject /* this */,
        jlong matAddrInput,
        jlong matAddrResult) {

    try {
        // Get the input and output matrices
        Mat &input = *(Mat *) matAddrInput;
        Mat &result = *(Mat *) matAddrResult;
        
        // Convert to grayscale
        Mat gray;
        cvtColor(input, gray, COLOR_RGBA2GRAY);
        
        // Apply Gaussian blur to reduce noise
        Mat blurred;
        GaussianBlur(gray, blurred, Size(5, 5), 1.5);
        
        // Apply Canny edge detection
        Mat edges;
        Canny(blurred, edges, 50, 150);
        
        // Convert back to RGBA for display
        cvtColor(edges, result, COLOR_GRAY2RGBA);
        
    } catch (const cv::Exception &e) {
        LOGE("OpenCV error: %s", e.what());
    } catch (...) {
        LOGE("Unknown error in native code");
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_edgevision_NativeProcessor_initOpenCV(JNIEnv *env, jobject /* this */) {
    try {
        // This is a simple check to ensure OpenCV is loaded
        cv::Mat testMat = cv::Mat::zeros(10, 10, CV_8UC1);
        return JNI_TRUE;
    } catch (const cv::Exception &e) {
        LOGE("OpenCV initialization failed: %s", e.what());
        return JNI_FALSE;
    } catch (...) {
        LOGE("Unknown error during OpenCV initialization");
        return JNI_FALSE;
    }
}

} // extern "C"
