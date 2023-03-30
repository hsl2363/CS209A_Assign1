import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
            Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  // 1
  public Map<String, Integer> getPtcpCountByInst() {
    Map<String, Integer> M = courses.stream()
        .collect(Collectors.groupingBy(Course::getInstitution, TreeMap::new,
            Collectors.summingInt(e -> e.participants)));
    return M;
  }

  // 2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Map<String, Integer> map = courses.stream()
        .collect(Collectors.groupingBy(Course::getInstAndSubject, TreeMap::new,
            Collectors.summingInt(e -> e.participants)));
    Comparator<String> cmp = (x, y) -> {
      Integer v1 = map.get(x);
      Integer v2 = map.get(y);
      if (v2.compareTo(v1) == 0) {
        return x.compareTo(y);
      } else {
        return v2.compareTo(v1);
      }
    };
    Map<String, Integer> M = new TreeMap<>(cmp);
    map.forEach((k, v) -> {
      M.put(k, v);
    });
    return M;
  }

  // 3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> M = new TreeMap<>();
    List<String> E = new ArrayList<>();
    Map<String, List<String>> ind = new TreeMap<>();
    Map<String, List<String>> cop = new TreeMap<>();
    String[] ins;
    String title;
    for (int i = 0; i < courses.size(); i++) {
      ins = courses.get(i).instructors.split(", ");
      title = courses.get(i).title;
      if (ins.length == 1) {
        if (!ind.containsKey(ins[0])) {
          ind.put(ins[0], new ArrayList<String>());
        }
        if (ind.get(ins[0]).contains(title) == false) {
          ind.get(ins[0]).add(title);
        }
      } else {
        for (int j = 0; j < ins.length; j++) {
          if (!cop.containsKey(ins[j])) {
            cop.put(ins[j], new ArrayList<String>());
          }
          if (cop.get(ins[j]).contains(title) == false) {
            cop.get(ins[j]).add(title);
          }
        }
      }
    }

    Set<String> ID = new HashSet<>();
    ind.forEach((k, v) -> {
      v.sort((a, b) -> a.compareTo(b));
      ID.add(k);
    });
    cop.forEach((k, v) -> {
      v.sort((a, b) -> a.compareTo(b));
      ID.add(k);
    });
    for (String s : ID) {
      List<List<String>> tmp = new ArrayList<>();
      if (ind.containsKey(s)) {
        tmp.add(ind.get(s));
      } else {
        tmp.add(E);
      }
      if (cop.containsKey(s)) {
        tmp.add(cop.get(s));
      } else {
        tmp.add(E);
      }
      M.put(s, tmp);
    }
    M.forEach((k, v) -> {
      System.out.println(k);
      System.out.println(v);
    });
    return M;
  }

  // 4
  public List<String> getCourses(int topK, String by) {
    Set<String> mk = new HashSet<>();
    List<String> res = new ArrayList<>();
    if (by.charAt(0) == 'h') {
      res = courses.stream()
          .sorted(Comparator.comparing(Course::getHours).reversed())
          .filter((a) -> mk.add(a.title))
          .map(s -> s.title)
          .limit(topK)
          .collect(Collectors.toList());
    } else {
      res = courses.stream()
          .sorted(Comparator.comparing(Course::getParti).reversed())
          .filter((a) -> mk.add(a.title))
          .map(s -> s.title)
          .limit(topK)
          .collect(Collectors.toList());
    }
    return res;
  }

  // 5
  public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
    Set<String> mk = new HashSet<>();
    List<String> res = new ArrayList<>();
    String str = courseSubject.toLowerCase();
    res = courses.stream()
        .filter(s -> s.percentAudited >= percentAudited && s.totalHours <= totalCourseHours
            && s.subject.toLowerCase().contains(str))
        .sorted(Comparator.comparing(Course::getTitle))
        .filter(a -> mk.add(a.title))
        .map(s -> s.title)
        .collect(Collectors.toList());
    return res;
  }

  // 6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Set<String> mk = new HashSet<>();
    List<String> res = new ArrayList<>();
    Map<String, List<Course>> map = courses.stream()
        .collect(Collectors.groupingBy(Course::getNumber));
    Map<String, Course> M = new TreeMap<>();
    map.forEach((k, v) -> {
      Course c = v.get(0);
      double Age = c.medianAge;
      double Gender = c.percentMale;
      double Bachelor = c.percentDegree;
      for (int i = 1; i < v.size(); i++) {
        Age += v.get(i).medianAge;
        Gender += v.get(i).percentMale;
        Bachelor += v.get(i).percentDegree;
        if (v.get(i).launchDate.after(c.launchDate)) {
          c = v.get(i);
        }
      }
      Age /= v.size();
      Gender /= v.size();
      Bachelor /= v.size();
      c.similarity = (age - Age) * (age - Age)
          + (gender * 100 - Gender) * (gender * 100 - Gender)
          + (isBachelorOrHigher * 100 - Bachelor) * (isBachelorOrHigher * 100 - Bachelor);
      M.put(k, c);
    });
    res = M.entrySet().stream()
        .sorted(Map.Entry.<String, Course>comparingByValue(Comparator.comparing(Course::getSimilarity))
            .thenComparing(Map.Entry.comparingByValue(Comparator.comparing(Course::getTitle))))
        .filter(a -> mk.add(a.getValue().title))
        .limit(10)
        .map(s -> s.getValue().title)
        .collect(Collectors.toList());
    return res;
  }

}

class Course {
  String institution;
  String number;
  Date launchDate;
  String title;
  String instructors;
  String subject;
  int year;
  int honorCode;
  int participants;
  int audited;
  int certified;
  double percentAudited;
  double percentCertified;
  double percentCertified50;
  double percentVideo;
  double percentForum;
  double gradeHigherZero;
  double totalHours;
  double medianHoursCertification;
  double medianAge;
  double percentMale;
  double percentFemale;
  double percentDegree;
  double similarity;

  public Course(String institution, String number, Date launchDate,
      String title, String instructors, String subject,
      int year, int honorCode, int participants,
      int audited, int certified, double percentAudited,
      double percentCertified, double percentCertified50,
      double percentVideo, double percentForum, double gradeHigherZero,
      double totalHours, double medianHoursCertification,
      double medianAge, double percentMale, double percentFemale,
      double percentDegree) {
    this.institution = institution;
    this.number = number;
    this.launchDate = launchDate;
    if (title.startsWith("\"")) {
      title = title.substring(1);
    }
    if (title.endsWith("\"")) {
      title = title.substring(0, title.length() - 1);
    }
    this.title = title;
    if (instructors.startsWith("\"")) {
      instructors = instructors.substring(1);
    }
    if (instructors.endsWith("\"")) {
      instructors = instructors.substring(0, instructors.length() - 1);
    }
    this.instructors = instructors;
    if (subject.startsWith("\"")) {
      subject = subject.substring(1);
    }
    if (subject.endsWith("\"")) {
      subject = subject.substring(0, subject.length() - 1);
    }
    this.subject = subject;
    this.year = year;
    this.honorCode = honorCode;
    this.participants = participants;
    this.audited = audited;
    this.certified = certified;
    this.percentAudited = percentAudited;
    this.percentCertified = percentCertified;
    this.percentCertified50 = percentCertified50;
    this.percentVideo = percentVideo;
    this.percentForum = percentForum;
    this.gradeHigherZero = gradeHigherZero;
    this.totalHours = totalHours;
    this.medianHoursCertification = medianHoursCertification;
    this.medianAge = medianAge;
    this.percentMale = percentMale;
    this.percentFemale = percentFemale;
    this.percentDegree = percentDegree;
  }

  public String getNumber() {
    return this.number;
  }

  public String getInstitution() {
    return this.institution;
  }

  public String getInstAndSubject() {
    return this.institution + '-' + this.subject;
  }

  public String getTitle() {
    return this.title;
  }

  public double getHours() {
    return this.totalHours;
  }

  public int getParti() {
    return this.participants;
  }

  public double getSimilarity() {
    return this.similarity;
  }

}