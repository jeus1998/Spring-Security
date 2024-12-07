package spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MethodController {
    private final DataService dataService;
    @GetMapping("/")
    public String index(){
        return "index";
    }
    @PostMapping("/writeList")
    public List<Account> writeList(@RequestBody List<Account> data) {
        return dataService.writeList(data);
    }
    @PostMapping("/writeMap")
    public Map<String, Account> writeMap(@RequestBody List<Account> data) {
        Map<String, Account> dataMap =
                data.stream().collect(Collectors.toMap(Account::getOwner, account -> account));
        return dataService.writeMap(dataMap);
    }
    @GetMapping("/readList")
    public List<Account> readList() {
        return dataService.readList();
    }
    @GetMapping("/readMap")
    public Map<String, Account> readMap() {
        return dataService.readMap();
    }
}
