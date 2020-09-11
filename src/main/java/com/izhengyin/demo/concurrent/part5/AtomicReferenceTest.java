package com.izhengyin.demo.concurrent.part5;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
/**
 * @author zhengyin zhengyinit@outlook.com
 * Created on 2020-09-07 18:52
 */
public class AtomicReferenceTest {

    public static void main(String[] args){
        Coord coord = new Coord(1.1,5.1);

        Coord newCoord = new Coord(2.1,6.1);
        AtomicReference<Coord> coordAtomicReference1 = new AtomicReference<>(coord);
        //成功
        System.out.println("compareAndSet ["+coordAtomicReference1.compareAndSet(coord,newCoord)+"] , new value "+coordAtomicReference1.get());

        Coord coord2 = new Coord(1.1,5.1);
        AtomicReference<Coord> coordAtomicReference2 = new AtomicReference<>(coord);
        //失败
        System.out.println("compareAndSet ["+coordAtomicReference2.compareAndSet(coord2,newCoord)+"] , equals ["+(coord2.equals(coord))+"] , new value "+coordAtomicReference2.get());
    }

    @Data
    @ToString
    @AllArgsConstructor
    private static class Coord {
        private double x;
        private double y;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()){
                return false;
            }
            Coord coord = (Coord) o;
            return Double.compare(coord.x, x) == 0 &&
                    Double.compare(coord.y, y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

}
