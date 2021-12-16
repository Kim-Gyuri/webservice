# :pushpin: 스프링 부트와 AWS로 구현하는 웹 서비스
+ 실습 도중에 어려웠던 것들, 겪었던 오류들, 배운 것을 

## Table of contents 목차
+ 실습 도중에 어려웠던 것들<br>
[1. 내 PC에서 RDS에 접속하기](#1-내-PC에서-RDS에-접속하기)<br>
[2. 배포 스크립트 만들기](#2-배포-스크립트-만들기)<br>
+ 겪었던 오류들<br>
[5. Backslash Escapes 백슬래쉬 이스케이프](#5-backslash-escapes-백슬래쉬-이스케이프)<br>
[6. Images 이미지](#6-images-이미지)<br>


## 1. 내 PC에서 RDS에 접속하기
* 오류설명: <br> 인텔리제이에서 SQL 쿼리 수행 중, 'RDS 파라미터 그룹 변경' 쿼리 실행이 안된다.<br>
원인은 모르겠지만 Execution Console에 variables 테이블표가 출력 안되었다.<br>
![289](https://user-images.githubusercontent.com/57389368/146322499-f08807bc-de6b-4e4f-b40d-d66dabf11477.JPG) <br>
* 해결 방법: ec2 리눅스(putty)로 SQL 쿼리를 실행해 변경했다.
* SQL <br>
```
mysql -u [계정] -p -h host주소
show variables like 'c%';
ALTER DATABASE 데이터베이스명
CHARACTER SET = 'utf8mb4'
COLLATE = 'utf8mb4_general_ci';
select @@time_zone, now();
```
>계정: 인텔리제이에서 RDS 접속 정보 등록했던 User를 적는다.<br>
>host 주소: RDS의 엔드포엔트 주소<br>
>다음 페이지 290의 '테스트 테이블 생성하기'도 ec2 리눅스로 실행시켜 확인했다.<br>

## 2. 배포 스크립트 만들기 
### ./gradlew test 실행 중 오류 <br>
* 오류 화면<br>
![javahome못찾아](https://user-images.githubusercontent.com/57389368/146330020-be34cccd-6f3f-447d-8932-5b003da14bc6.JPG)<br>
* 오류 원인: ec2 리눅스에 자바설치를 안했기 때문에 발생하였다.<br>
* 해결 방법: ec2 리눅스에 자바 설치를 한다. --> 페이지(258)에 아마존 리눅스 서버 생성 시 필수설정 내용이 있다 <br>
```
-설치: sudo yum install java-11-amazon-corretto
-설치확인: sudo alternatives --config java
-자바 버전 확인: java -version
-환경 변수 설정: sudo vi /etc/profile (sudo를 적지 않으면 readOnly파일로 들어가짐, 편집 안됨 주의)
```
>참고 자료: <br>
[자바 설치](https://docs.aws.amazon.com/ko_kr/corretto/latest/corretto-11-ug/amazon-linux-install.html)<br>
[환경 변수 설정](https://kitty-geno.tistory.com/25)<br>

### 자바를 설치했지만 또 다시 ./gradlew test 오류발생 <br>
* 오류 원인: 5장 '기존 테스트에 시큐리티 적용하기'를 해결 못했기 때문이다. <br>
* 해결 방법:
```
1. 페이지(214p)의 'src/test/resources/application.properties'를 추가하기
2. 그러나 Spring Boot가 2.1 -> 2.4로, IntelliJ IDEA가 2019 -> 2020으로 오면서 버전오류가 있음
3. 아래와 같이 수정한다.
```

* src/main/resources/application.properties
```
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL57Dialect
spring.jpa.properties.hibernate.dialect.storage_engine=innodb
spring.datasource.hikari.jdbc-url=jdbc:h2:mem:testdb;MODE=MySQL;
spring.h2.console.enabled=true


spring.session.store-type=jdbc

spring.config.use-legacy-processing=true

spring.profiles.include=oauth
```

* src/test/resources/application.properties
```
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL57Dialect
spring.jpa.properties.hibernate.dialect.storage_engine=innodb
spring.datasource.hikari.jdbc-url=jdbc:h2:mem:testdb;MODE=MySQL;
spring.h2.console.enabled=true



spring.security.oauth2.client.registration.google.client-id=test
spring.security.oauth2.client.registration.google.client-secret=test
spring.security.oauth2.client.registration.google.scope=profile,email
```

>참고 자료: <br>
[(2020.12.16) 스프링 부트와 AWS로 혼자 구현하는 웹 서비스 최신 코드로 변경하기](https://jojoldu.tistory.com/539)<br>




### deploy.sh 파일 작성 중, 책 오타 오류  <br>
* () 괄호로 수정한다. <br>
```
CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)
```
