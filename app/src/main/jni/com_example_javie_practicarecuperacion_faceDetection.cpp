#include "com_example_javie_practicarecuperacion_faceDetection.h"
JNIEXPORT void JNICALL Java_com_example_javie_practicarecuperacion_faceDetection_faceDetection
  (JNIEnv *, jclass, jlong addrRgba){
    Mat& frame = *(Mat*)addrRgba;
    detect(frame);
  }

  void detect(Mat& frame){

  }