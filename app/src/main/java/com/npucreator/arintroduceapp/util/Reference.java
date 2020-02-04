package com.npucreator.arintroduceapp.util;

public class Reference
{
    public static final int SHOW = 0;
    public static final int HIDE = 1;

    public static class ModelMatrix
    {
        public float[] matrix = new float[16];
        public ModelMatrix(float[] matrix)
        {
            this.matrix = matrix;
            //matrix[0] = -matrix[0];
            //matrix[10] = -matrix[10];
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < 15; i++) {
                sb.append(matrix[i]);
                sb.append(',');
            }
            sb.append(matrix[15]);

            /*for (int i = 1; i <= 4; i++)
            {
                for (int j = 1; j <= 4; j++)
                {
                    sb.append(matrix[i-1+(j-1)*4]);
                    if (!(i==4 && j == 4))
                        sb.append(',');
                }
            }*/

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

        ModelMatrix matrix1 = new ModelMatrix(matrix);

        System.out.println(matrix1.toString());
    }
}
