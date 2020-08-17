package test.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("1","1");

        ConcurrentHashMap<String, String> map2 = new ConcurrentHashMap<>();
        map2.putIfAbsent("1","1");

    }
}
