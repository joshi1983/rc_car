#include "opencv2/highgui/highgui.hpp"
#include "opencv2/opencv.hpp"
#include <iostream>

using namespace cv;
using namespace std;

int main(int argc, char* argv[]){
    VideoCapture vCap(0);
    VideoWriter vWrite;
    vWrite.open("test.avi", vWrite.fourcc('M','J','P','G'), 20, Size(640, 480), true);
    while (1) {
        namedWindow("VideoFeed", WINDOW_AUTOSIZE);
        Mat frame;
        vCap.read(frame);
        vWrite.write(frame);
        imshow("VideoFeed", frame);
        char c = waitKey(50);
        if (c == 27) {
            break;
        }
    }
    return 0;
}
