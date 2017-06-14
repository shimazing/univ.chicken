package uc;

import org.nd4j.linalg.factory.Nd4j;
import uc.UCConfiguration;
import uc.data.KNNLRUCache;
import uc.data.QecTable;

import java.io.File;
import java.util.Random;

/**
 * Created by keltp on 2017-06-14.
 */
public class Test3 {
    public static void main(String[] args) throws Exception {
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0;i < 100;i++)
            System.out.println(r.nextDouble());


    }
}
