
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MovieAnalyzer {
    private List<String[]> data = new ArrayList<>();

    public MovieAnalyzer(String dataset_path) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(dataset_path))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<String[]> getData() {
        return data;
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> yearCount = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            int key = Integer.parseInt(getData().get(i)[2]);
            if (yearCount.containsKey(key)) {
                int value = yearCount.get(key);
                yearCount.put(key, ++value);
            } else {
                yearCount.put(key, 1);
            }
        }

        LinkedHashMap<Integer, Integer> result = new LinkedHashMap<>();
        List<Map.Entry<Integer, Integer>> entryList = new ArrayList<>(yearCount.entrySet());
        entryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(entryList);
        for (Map.Entry<Integer, Integer> integerIntegerEntry : entryList) {
            result.put(integerIntegerEntry.getKey(), integerIntegerEntry.getValue());
        }
        return result;
    }

    public Map<String, Integer> getMovieCountByGenre() {
        Map<String, Integer> genreCount = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            String genre = getData().get(i)[5];
            genre = genre.replaceAll("\"", "");
            String[] keyArray = genre.split(", ");
            for (String key : keyArray) {
                if (genreCount.containsKey(key)) {
                    int value = genreCount.get(key);
                    genreCount.put(key, ++value);
                } else {
                    genreCount.put(key, 1);
                }
            }
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(genreCount.entrySet());
        entryList.sort(Map.Entry.comparingByKey());
        Collections.reverse(entryList);
        entryList.sort(Map.Entry.comparingByValue());
        Collections.reverse(entryList);
        for (Map.Entry<String, Integer> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<List<String>, Integer> coStarCount = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            ArrayList<String> stars = new ArrayList<>(Arrays.asList(data.get(i)).subList(10, 14));
            Collections.sort(stars);
            for (int m = 0; m < stars.size(); m++) {
                for (int n = m + 1; n < stars.size(); n++) {
                    List<String> coStar = new ArrayList<>(Arrays.asList(stars.get(m), stars.get(n)));
                    if (coStarCount.containsKey(coStar)) {
                        int value = coStarCount.get(coStar);
                        coStarCount.put(coStar, ++value);
                    } else {
                        coStarCount.put(coStar, 1);
                    }
                }
            }
        }
        LinkedHashMap<List<String>, Integer> result = new LinkedHashMap<>();
        List<Map.Entry<List<String>, Integer>> entryList = new ArrayList<>(coStarCount.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        Collections.reverse(entryList);

        for (Map.Entry<List<String>, Integer> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    public List<String> getTopMovies(int top_k, String by) {
        List<String> result = new ArrayList<>();
        List<String[]> list = new ArrayList<>(data);
        list.remove(0);
        if (by.equals("runtime")) {
            list.sort(Comparator.comparing(o -> o[1].replaceAll("\"", "")));
            Collections.reverse(list);
            list.sort(Comparator.comparing(o -> Integer.parseInt(o[4].replaceAll(" min", ""))));
            Collections.reverse(list);

            list.stream()
                .limit(top_k)
                .forEach(e -> result.add(e[1].replaceAll("\"", "")));
        } else {
            list.sort(Comparator.comparing(o -> o[1].replaceAll("\"", "")));
            Collections.reverse(list);
            list.sort(Comparator.comparing(o -> (o[7].replaceAll("(^\")|(\"$)", "")).length()));
            Collections.reverse(list);

            list.stream()
                    .limit(top_k)
                    .forEach(e -> result.add(e[1].replaceAll("\"", "")));
        }
        return result;
    }

    public List<String> getTopStars(int top_k, String by) {
        List<String> result = new ArrayList<>();

        if (by.equals("rating")) {
            Map<String, Double> Top = new HashMap<>();
            Map<String, Integer> num = new HashMap<>();
            List<String> stars = new ArrayList<>();
            for (int i = 1; i < data.size(); i++) {
                List<String> Key = new ArrayList<>(Arrays.asList(data.get(i)[10], data.get(i)[11], data.get(i)[12], data.get(i)[13]));
                double nextValue = Float.parseFloat(data.get(i)[6]);
                for (String key : Key) {
                    if (Top.containsKey(key)) {
                        double value = Top.get(key);
                        int times = num.get(key);
                        Top.put(key, value + nextValue);
                        num.put(key, ++times);
                    } else {
                        Top.put(key, nextValue);
                        stars.add(key);
                        num.put(key, 1);
                    }
                }
            }

            stars.forEach(e -> Top.put(e, Top.get(e) / num.get(e)));

            List<Map.Entry<String, Double>> entryList = new ArrayList<>(Top.entrySet());
            entryList.sort(Map.Entry.comparingByKey());
            Collections.reverse(entryList);
            entryList.sort(Map.Entry.comparingByValue());
            Collections.reverse(entryList);
            entryList.stream()
                    .limit(top_k)
                    .forEach(s -> result.add(s.getKey()));
        } else {
            Map<String, Long> Top = new HashMap<>();
            Map<String, Integer> num = new HashMap<>();
            List<String> stars = new ArrayList<>();
            for (int i = 1; i < data.size(); i++) {
                List<String> Key = new ArrayList<>(Arrays.asList(data.get(i)[10], data.get(i)[11], data.get(i)[12], data.get(i)[13]));
                long nextValue;
                if (data.get(i).length == 16) {
                    nextValue = Long.parseLong(data.get(i)[15].replaceAll(",", "").replaceAll("(^\")|(\"$)", ""));
                } else {
                    continue;
                }
                for (String key : Key) {
                    if (Top.containsKey(key) && nextValue != 0) {
                        long value = Top.get(key);
                        int times = num.get(key);
                        Top.put(key, value + nextValue);
                        num.put(key, ++times);
                    } else {
                        Top.put(key, nextValue);
                        num.put(key, 1);
                        stars.add(key);
                    }
                }
            }

            stars.forEach(e -> Top.put(e, Top.get(e) / num.get(e)));
            List<Map.Entry<String, Long>> entryList = new ArrayList<>(Top.entrySet());
            entryList.sort(Map.Entry.comparingByKey());
            Collections.reverse(entryList);
            entryList.sort(Map.Entry.comparingByValue());
            Collections.reverse(entryList);
            entryList.stream()
                    .limit(top_k)
                    .forEach(s -> result.add(s.getKey()));
        }
        return result;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        List<String> result = new ArrayList<>();
        data.stream()
                .skip(1)
                .filter(s -> Float.parseFloat(s[6]) >= min_rating)
                .filter(s -> Integer.parseInt(s[4].replaceAll(" min", "")) <= max_runtime)
                .filter(s -> s[5].contains(genre))
                .forEach(s -> result.add(s[1].replaceAll("\"", "")));
        Collections.sort(result);
        return result;
    }
}
