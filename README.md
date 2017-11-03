# licenseplate-recognition

A simple application that detects license plates and reads them.
Using JavaFX to display the progress/results.

-- OpenCV 3.3 for image morphology to isolate the region with the license plate
   - It's necessary to install opencv on your environment / OS since the jar is just a JNI wrapper, 
     actual execution uses cpp (GCC)
   - Once you've built your opencv-{version}.jar on your local machine,
     move the built opencv jar to ~/.m2/repository/org/opencv/opencv/{version} folder so it can be imported through maven
-- Tess4j (tessaract) as OCR engine 
   - Tesseract is a standalone engine which does the OCR,
     so it's necessary to install it to your environment / OS:
     - sudo apt-get install tesseract-ocr // for debian based os
     - sudo yaourt -S tesseract // for arch based os
     - download the trained character data from https://github.com/tesseract-ocr/tessdata/blob/master/eng.traineddata
       and move it to /usr/share/tessdata/
     - necessary to set TESSDATA_PREFIX as the environment variable which will point to /usr/share/tessdata/ (or your location of traineddata)
