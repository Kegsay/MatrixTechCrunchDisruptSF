#!/usr/bin/env python
import distutils.dir_util
import os
import shutil
import sys
from xml.dom import minidom

def convert(unity_dir, android_dir, unity_lib_dir):
    print "Unity-generated android dir: %s" % unity_dir
    print "Android project dir: %s" % android_dir
    print "Unity android library dir: %s" % unity_lib_dir       
    
    print "Cleaning up existing temp stuff.."
    unity_temp_dir = "unity-temp-dir"
    try:
        shutil.rmtree(unity_temp_dir)
    except:
        pass
    print "Done."
    
    print "Stealing android files from unity-generated directory %s ....."
    shutil.copytree(unity_dir, unity_temp_dir)
    print "Done."
    
    print "Linking classes.jar from unity android library %s" % unity_lib_dir
    abs_path_classes_jar = os.path.abspath(os.path.join(unity_lib_dir, "development/bin/classes.jar"))
    added = False
    dom = None
    with open(os.path.join(android_dir, ".classpath"), 'r') as f:
        dom = minidom.parseString(f.read())
        class_path_entries = dom.getElementsByTagName("classpathentry")
        for class_path in class_path_entries:
            path = class_path.getAttribute("path")
            abs_path = os.path.abspath(path)
            if abs_path == abs_path_classes_jar:
                print "Already linked classes.jar in .classpath"
                added = True
                break
    if not added:
        print "FAIL: Update .classpath to include %s" % abs_path_classes_jar
        sys.exit(1)
            
    print "Moving unity-generated android assests to actual android project..."
    distutils.dir_util.copy_tree(unity_temp_dir+"/assets", android_dir+"/assets")
    print "Done."
    
            


def main():
    unity_dir = None
    android_dir = None
    unity_lib_dir = None
    with open("config", "r") as f:
        lines = f.readlines()
        for line in lines:
            key_vals = line.split("=")
            if "unity-temp" == key_vals[0]:
                unity_dir = key_vals[1].strip()
            elif "android" == key_vals[0]:
                android_dir = key_vals[1].strip()
            elif "unity-lib" == key_vals[0]:
                unity_lib_dir = key_vals[1].strip()
    convert(unity_dir, android_dir, unity_lib_dir)

if __name__ == '__main__':
    main()