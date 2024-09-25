# CORS (Cross Origin Resource Sharing)  2

## CORS 테스트 

### 테스트 모듈1 - cors1

- 루트 폴더를 마우스 오른쪽으로 클릭 new/Module
- Spring initializr 선택 
- 이름만 cors1 설정 나머지 그대로 & next 클릭 
- 의존성: Spring Web, Thymeleaf 선택 
- application yaml: 포트 변경 8081

![7.png](Image%2F7.png)


resources/templates/index.html 추가 
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <script>

        function corsTest(){
            fetch("http://localhost:8082/api/users",{
                method : "GET",
                headers : {
                    "Content-Type" : "text/xml",
                }
            })
                .then(response => {
                    response.json().then(function(data){
                        console.log(data)
                    })
                })
        }

    </script>
</head>
<body>
<button name="corsTest" onclick="corsTest()">corsTest</button>
</body>
</html>
```

컨트롤러 
```java
@Controller
public class CorsController {
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
```

### 테스트 모듈2 - cors2

- 루트 폴더를 마우스 오른쪽으로 클릭 new/Module
- Spring initializr 선택 
- 이름만 cors2 설정 나머지 그대로 & next 클릭 
- 의존성: Spring Web, Spring Security 선택 
- application yaml: 포트 변경 8082

CorsController
```java
@RestController
@RequestMapping("/api")
public class CorsController {
    @GetMapping("/users")
    public String users(){
        return "{\"name\":\"hong gil dong\"}";
    }
}

```

![8.png](Image%2F8.png)

### 테스트1

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http    .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8083"); // 허용 출처
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
```
- CORS1 서버 허용 X 포트 차이 
  - CORS1 서버 : 8081 포트 현재 허용: 8083 

![9.png](Image%2F9.png)


### 테스트2

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http    .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8081"); // 허용 출처
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
```
- CORS1 서버 허용 O  
  - CORS1 서버 : 8081 포트 현재 허용: 8081 

![10.png](Image%2F10.png)
- 요청을 보면 캐시 이전에 Preflight 먼저 요청을 하고 그 이후에 본 요청을 한다. 

### CORS 필터 동작 

```text
CORS 필터는 필터 체인 제일 앞에서 동작 
만약 현재 요청이 SimpleRequest(본 요청) 아닌 예비 요청(Preflight) 이라면 다음 Filter 로 넘기지 않고 

doFilter() X

리턴한다. 
```
