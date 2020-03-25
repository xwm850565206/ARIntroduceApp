package com.npucreator.arintroduceapp.util;

import android.util.Log;

import java.util.Arrays;

public class Reference
{
    public static final int SHOW = 0;
    public static final int HIDE = 1;

    public enum DetectType {
        t111, t112, t113, t114, t115, t116,
        t121, t122, t123, t124, t125, t126,
        t131, t132, t133, t134;

        /*t211, t212, t213, t214, t215,
        t22,
        t23,
        t241, t242, t243, t244, t245, t246, t247, t248,

        t30,
        t311, t312, t313, t314, t315,
        t321, t322, t323, t324, t325, t326, t327, t328,

        t330, t331, t332, t333, t334, t335, t336, t337, t338,
        t340, t341*/

        public static DetectType getType(String typename){

            typename = typename.split("/")[1]; // 输入的 typename 是 'data/' + type 的形式

            if (!typename.startsWith("t"))
                typename = 't' + typename;

            DetectType[] types = DetectType.values();
            for (DetectType type : types)
            {
                if (type.toString().equals(typename))
                    return type;
            }

            return null;
        }
    }

    /**
     * 将1*16的float数组封装成opengl的变换矩阵，与unity中的脚本进行通讯
     * 同时还携带当前检测到的是哪个图片信息
     */
    public static class DetectPacket
    {
        float[] matrix; // 变化矩阵 传回给unity进行处理
        public DetectType detectType; // 检测到的类型

        public DetectPacket(float[] matrix)
        {
            this.matrix = matrix;
        }
        public DetectPacket(float[] matrix, DetectType type){
            this(matrix);
            this.detectType = type;
        }

        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < 15; i++) {
                sb.append(matrix[i]);
                sb.append(',');
            }
            sb.append(matrix[15]);

            return sb.toString();
        }

        public String getPosition(){
            //todo
            return "";
        }

        public String getSize(){
            //todo
            return "";
        }

        public String getRotation(){
            //todo
            return "";
        }
    }

    public static void main(String[] args)
    {
        float[] matrix = new float[16];

        for (int i = 0; i < 16; i++)
            matrix[i] = i+1;

        DetectPacket packet = new DetectPacket(matrix, DetectType.t111);

        System.out.println(Arrays.asList(DetectType.values()).toString());

        System.out.println(packet.toString());
    }
}
