# :pushpin: 스프링 부트와 AWS로 구현하는 웹 서비스
+ 실습 도중에 어려웠던 것들, 겪었던 오류들, 배운 것을 

## Table of contents 목차
+ 실습 도중에 어려웠던 것들<br>
[1. Amazon Linux 인스턴스의 호스트 이름 변경](#1-Amazon-Linux-인스턴스의-호스트-이름-변경)<br>
[2. 내 PC에서 RDS에 접속하기](#2-내-PC에서-RDS에-접속하기)<br>
[3. 배포 스크립트 만들기](#3-배포-스크립트-만들기)<br>

## 1. Amazon Linux 인스턴스의 호스트 이름 변경
* Hostname 변경 : <br>
```
sudo hostnamectl set-hostname [변경할 이름]
```
>2020년 11월 23일 기준 Amazon Linux AMI로는 인스턴스 생성이 불가능하고 Amazon Linux2로만 가능합니다. <br>
>amazon linux2에서는 호스트명 변경 방법이 달라서 책에 기재된 방법으로는 변경이 안되고 hostnamectl 명령어를 이용해서 변경해야 합니다. <br>
>[Linux 인스턴스용 사용 설명서 :Hostname 변경](https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/set-hostname.html)<br>

## 2. 내 PC에서 RDS에 접속하기
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

## 3. 배포 스크립트 만들기 
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




### deploy.sh 파일 작성 중, 책 오타 수정  <br>
* () 괄호로 수정한다. <br>
```
CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)
```

### deploy 실행시 Unable to access jarfile 오류
* 오류 화면
![305error](https://user-images.githubusercontent.com/57389368/146358195-ad408647-31ba-4bfc-9b9d-f2c4dd02780d.JPG) <br>
* 외부 Security 파일 등록하기 (304 페이지)부터 실행하는 도중 발생한 오류이다.
* 오류분석: 마지막 'nohup java..' 내용의 Jar 위치를 인식을 못해 발생한 듯 싶었다.
* 해결방법 : <br>
  아래와 같이 deploy.sh 파일을 수정한다. 
  \ 없이 한줄에 다 붙여서, 이때 엔터나 기타 다른 문자가 포함되어선 안된다.
``` 
nohup java -jar -Dspring.config.location=classpath:/application.properties,/home/ec2-user/app/application-oauth.properties /home/ec2-user/app/step1/$JAR_NAME 2>&1 &
```
>참고자료: [deploy 실행시 Unable to access jarfile 오류](https://github.com/jojoldu/freelec-springboot2-webservice/issues/168)<br>

### 스프링 부트 프로젝트로 RDS 접근하기
* 아래 'create table'부터 복사하여 RDS에 반영한다.
![304페이지 오류없이 그냥 성공됨](https://user-images.githubusercontent.com/57389368/146359810-78a64b6c-4fe6-4839-80e1-434dfac33f4d.JPG) 
![308 세션 테이블](https://user-images.githubusercontent.com/57389368/146360179-ad26133e-f9c9-473e-a01e-d00a1133bd46.JPG) <br>
* ec2에서 RDS 접속 후 쿼리를 실행한다.
```
mysql -u [계정] -p -h host주소
show databases;  #해당 데이터 베이스 검색
use [해당 rds 지정한 데이터베이스명]

create table posts (id bigint not null auto_increment, created_date datetime, modified_date datetime, author varchar(255), content TEXT not null, title varchar(500) not null, primary key (id)) engine=InnoDB;
create table user (id bigint not null auto_increment, created_date datetime, modified_date datetime, email varchar(255) not null, name varchar(255) not null, picture varchar(255), role varchar(255) not null, primary key (id)) engine=InnoDB;

CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BLOB NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;
```

* src/main/resources/application-real.properties
```
spring.profiles.include=oauth,real-db
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
spring.session.store-type=jdbc
```

*
```
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show_sql=false

spring.datasource.hikari.jdbc-url=jdbc:mariadb://rds엔드포인트주소:(기본포트)3306/RDS DB 인스턴스 이름
spring.datasource.hikari.username=DB 계정(user)
spring.datasource.hikari.password=DB 계정 비밀번호
spring.datasource.hikari.driver-class-name=org.mariadb.jdbc.Driver
```

* deploy.sh 수정
```
nohup java -jar \
    -Dspring.config.location=classpath:/application.properties,/home/ec2-user/app/application-oauth.properties,/home/ec2-user/app/application-real-db.properties \
    -Dspring.profiles.active=real \
    $REPOSITORY/$JAR_NAME 2>&1 &
```    
