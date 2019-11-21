package evo;




import java.util.*;

public class Operator {
    protected int cLength;
    protected static Random random = new Random();

    public static LinkedList<Integer> array2LinkedList(Integer[] c) {
        List<Integer> list = Arrays.asList(c);
        return new LinkedList<>(list);
    }

    public static int[] insertByTimes(int[] c, int times) {
        Integer[] copyc = int2Integer(c);
        LinkedList<Integer> linkedList = array2LinkedList(copyc);
        for (int i = 0; i < times; ++i) {
            insertByOne(linkedList);
        }
        Integer[] res = linkedList.toArray(copyc);
        return Integer2int(res);
    }

    protected static void insertByOne(LinkedList<Integer> c) {
        int[] pos = generateTwoDifferentInteger(c.size());
        insertByOne(c, pos[0], pos[1], false);
    }

    protected static LinkedList<Integer> insertByOne(LinkedList<Integer> c, int originalPos, int newPos, boolean returnWithNewOne) {
        /**
         * originalPos是原index
         * newPos是插入的index
         * returnWithNewOne 如果为true就返回一个复制的LinkedList，通常在调用时需要赋给新的变量
         *                  如果为false就返回原LinkedList,通常在调用时就不用赋值给新的变量
         */

        if (returnWithNewOne) {
            c = new LinkedList<Integer>(c);
        }
        Integer itemBeSelected = c.remove(originalPos);
        c.add(newPos, itemBeSelected);
        return c;
    }

    protected static int[] generateTwoDifferentInteger(int max) {
        int[] res = new int[2];
        res[0] = random.nextInt(max);
        res[1] = random.nextInt(max);
        while (res[1] == res[0])
            res[1] = random.nextInt(max);
        return res;
    }

    protected static Integer[] int2Integer(int[] array) {
        Integer[] res = new Integer[array.length];
        for (int i = 0; i < array.length; ++i) {
            res[i] = array[i];
        }
        return res;
    }

    protected static int[] Integer2int(Integer[] array) {
        int[] res = new int[array.length];
        for (int i = 0; i < array.length; ++i) {
            res[i] = array[i];
        }
        return res;
    }


    public static boolean[] swap(boolean[] arr, int number, boolean copy) {
        boolean[] res;
        if (copy) {
            res = Arrays.copyOf(arr, arr.length);
        } else {
            res = arr;
        }
        for (int i = 0; i < number; ++i) {
            int first = random.nextInt(arr.length);
            int second;
            //This is different from the another swap applied in int[] swap.
            //It delete the ||arr[second]==arr[first]
            while ((second = random.nextInt(arr.length)) == first) ;
            boolean temp = res[first];
            res[first] = res[second];
            res[second] = temp;
        }
        return res;
    }

    public static void swap(boolean[] arr, int number) {
        swap(arr, number, false);
    }

    public static void swap(int[] arr, int number) {
        swap(arr, number, false);
    }

    public static int[] swap(int[] arr, int number, boolean copy) {
        int[] res;
        if (copy) {
            res = Arrays.copyOf(arr, arr.length);
        } else {
            res = arr;
        }
        for (int i = 0; i < number; ++i) {
            int first = random.nextInt(arr.length);
            int second;
            while ((second = random.nextInt(arr.length)) == first || res[second] == res[first]) ;
            int temp = res[first];
            res[first] = res[second];
            res[second] = temp;
        }
        return res;
    }

    public static int[] crossover(int[] bsol, int[] sol, int orderNum) {
        if (bsol.length != sol.length) {
            throw new RuntimeException();
        }
        int first = random.nextInt(bsol.length);
        int second;
        while ((second = random.nextInt(bsol.length)) == first || Math.abs(second - first) > 0.5 * (bsol.length)) ;
        if (second < first) {
            int temp = first;
            first = second;
            second = temp;
        }
        int[] copyc = Arrays.copyOf(bsol, bsol.length);
        int[] store = new int[orderNum];
        for (int i = 0; i < copyc.length; ++i) {
            ++store[copyc[i]];
        }
        int[] tempStore = new int[orderNum];
        for (int i = first; i < second; ++i) {
            ++tempStore[copyc[i]];
        }
        int index = 0;
        for (int i = 0; i < first; ++i) {
            try {
                while (tempStore[sol[index]] >= store[sol[index++]]) ;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            copyc[i] = sol[index - 1];
            ++tempStore[copyc[i]];
        }
        for (int i = second; i < bsol.length; ++i) {
            try {
                while (tempStore[sol[index]] >= store[sol[index++]]) ;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            copyc[i] = sol[index - 1];
            ++tempStore[copyc[i]];
        }
        int temp = 0;
        while (temp < tempStore.length && tempStore[temp] == store[temp++]) ;
        if (temp != tempStore.length) throw new RuntimeException();
        return copyc;
    }


    /**
     * This method returns the crossover result of the two accept solution{@code acc1},{@code acc2}
     *
     * @param acc1 an accept solution
     * @param acc2 an accept solution
     * @return the crossOver result.
     */
    public static boolean[] crossOverSameAcceptNum(boolean[] acc1, boolean[] acc2, int accept) {
        final int length = acc1.length;
        boolean[] copyacc = new boolean[length];
        int curAccept = 0;
        for (int i = 0; i < length && curAccept < accept; ++i) {
            copyacc[i] = random.nextDouble() < 0.5 ? acc1[i] : acc2[i];
            if (copyacc[i]) ++curAccept;
        }
        while ((curAccept++) < accept) {
            int tempPos;
            do {
                tempPos = random.nextInt(length);
            } while (copyacc[tempPos]);
            copyacc[tempPos] = true;
        }
        return copyacc;

    }

    /**
     * if the AcceptNum of the two accept solution is different, then choose the middle point as the crossover acceptNum
     * @param acc1
     * @param acc2
     * @param acceptNum1
     * @param acceptNum2
     * @return
     */
    public static boolean[] crossOverDiffAcceptNum(boolean[] acc1, boolean[] acc2, int acceptNum1, int acceptNum2) {
        int accept = (acceptNum1 + acceptNum2) >> 1;
        return crossOverSameAcceptNum(acc1,acc2,accept);

    }

}

