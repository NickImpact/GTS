import com.google.common.collect.Comparators;
import com.google.common.collect.Lists;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SortingTests {

	public static void main(String[] args) {
		List<Double> test = new ArrayList<>();
		Random rng = new Random();
		for(int i = 0; i < 10; i++) {
			test.add(rng.nextDouble());
		}
		System.out.println(test.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));

		LocalDateTime x = LocalDateTime.now();
		LocalDateTime y = LocalDateTime.now().plusHours(3);
		LocalDateTime z = LocalDateTime.now().plusDays(2);
		LocalDateTime a = LocalDateTime.now().atZone(ZoneId.of("UTC")).toLocalDateTime().plusMinutes(5);
		List<LocalDateTime> list = Lists.newArrayList(x, y, z, a);
		System.out.println(list.stream().sorted().collect(Collectors.toList()));
		System.out.println(list.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()));
	}

}
