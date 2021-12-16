# :pushpin: 스프링 부트와 AWS로 구현하는 웹 서비스
+ 실습 도중에 어려웠던 것들, 겪었던 오류들, 배운 것들을 정리해보자.

## 목차
[0. EC2 Network Error Connection timeout 오류](#0-EC2-Network-Error-Connection-timeout-오류)<br>
[1. Amazon Linux 인스턴스의 호스트 이름 변경](#1-Amazon-Linux-인스턴스의-호스트-이름-변경)<br>
[2. 내 PC에서 RDS에 접속하기](#2-내-PC에서-RDS에-접속하기)<br>
[3. 배포 스크립트 만들기](#3-배포-스크립트-만들기)<br>
+ ./gradlew test 실행 중 오류
+ 자바를 설치했지만 또 다시 ./gradlew test 오류발생
+ deploy.sh 파일 작성 중, 책 오타 수정
+ deploy 실행시 Unable to access jarfile 오류
+ 스프링 부트 프로젝트로 RDS 접근하기
+ curl localhost:8080시 나타나는 에러

[4. Travis CI 연동하기](#4-Travis-CI-연동하기)<br>
[5. Git Actions으로 연동하기](#5-Git-Actions으로-연동하기)<br>
[5. Git Actions으로 연동하기](#5-Git-Actions으로-연동하기)<br>
[6. aws 프리 티어는 무료가 아니다](#6-aws-프리-티어는-무료가-아니다)<br>
[7. 생각 회고](#7-생각-회고)<br>

## 0. EC2 Network Error Connection timeout 오류
* 보안 그룹 > 인바운드 규칙 보기 탭으로 들어가서 SSH, 위치무관 규칙을 추가한다.
![ec2 위치무관](https://user-images.githubusercontent.com/57389368/146396869-50cb1229-910e-4c9c-ad60-653673e80d4d.JPG)

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

* src/main/resources/application-real-db.properties
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

### curl localhost:8080시 나타나는 에러
* 오류 분석: <br>
"단순히 생각해서 pid를 못 찾는 구나" 라고 생각했었다. 검색해보니 프로젝트의 deploy.sh에서 PID를 찾을 때 grep 으로 실행중인 jar 파일을 찾고 있었어서 PID를 찾지 못한것 같아 jar를 java로 바꿨더니 바로 PID를 찾을 수 있었다는 글을 읽고 deploy.sh를 고쳐보니 해결되었다. <br>
```
CURRENT_PID=$(pgrep -fl 프로젝트명 | grep java | awk '{print $1}') 
```
>[참고자료: pid 찾기](https://giters.com/jojoldu/freelec-springboot2-webservice/issues/688)<br>

## 4. Travis CI 연동하기
* travis-ci.org 2020년에 문 닫았으므로 'tavis-ci.com'으로 옮겨야 한다. [Travis](https://app.travis-ci.com)<br>
* Github - Travis 연동 에러 
![324캡처](https://user-images.githubusercontent.com/57389368/146386291-ce96329a-b79a-477c-b5e5-da5033e281b6.JPG)
* 2020년부터 Travis CI 서비스는 10,000 크레딧 제공에 차감식으로 지원하는 시스템으로 바뀌었다, <br>
  비자 카드를 등록하고 무료 플랜으로 가입해야만 깃허브 저장소 활성화가 가능하다. <br>


## 5. Git Actions으로 연동하기
* 최근 대세가 TravisCI에서 Github Action으로 넘어갔음이 느껴져 Github Action과 Beanstalk 조합으로 서버 배포하는 실습을 해본다.
>참고 <br>
>[1. Github Action & AWS Beanstalk 배포하기 - Github Action으로 빌드하기](https://jojoldu.tistory.com/543?category=777282)<br>
>[2. Github Action & AWS Beanstalk 배포하기 - profile=local로 배포하기](https://jojoldu.tistory.com/549?category=777282)<br>


## 6. aws 프리 티어는 무료가 아니다
* AWS EC2의 free tier를 지원하는 t2.micro을 사용하면서 지출되는 금액을 말한다, 나의 경우 아래와 같이 청구서를 받았다.
![aws 1](https://user-images.githubusercontent.com/57389368/146390590-63aaef0d-7cb9-4daf-85d2-eee42eda0c45.JPG)
![aws 2](https://user-images.githubusercontent.com/57389368/146390668-de7517f3-c2d0-40ee-ad26-2a5ab8d82337.JPG)
>참고자료: <br>
>[[AWS] Free-tier로 RDS 사용 중 요금](https://velog.io/@arara90/AWS-Free-tier%EB%A1%9C-RDS-%EC%82%AC%EC%9A%A9-%EC%A4%91-%EC%9A%94%EA%B8%88%EC%9D%84-%EC%A7%80%EB%B6%88%ED%96%88%EC%96%B4%EC%9A%94)<br>


## 7. 생각 회고
 * 책을 통한 학습이기 때문에 무엇보다 '실습 도구들의 버전업', '서비스 배포 관련 사이트(aws, travis) 정책변화' 등등 너무 많은 변화가 있다보니, 학습을 따라가기가 솔직히 힘들었었고 특히 aws 사용에 익숙하지 않아 많은 삽질을 했던 것 같다. <br>
* 자바나 gradle 버전에 따라 추가 설정이 있어야 정상실행이 될 수도 있다는 점을 다시 한 번 느꼈다. <br>
* travis 배포과정에서 비자카드가 없어 제 2의 루트 'Git Actions'으로 연동하려는 과정에서 'EB :severe' 문제를 겪었다. 해당 오류에 대해 구글링 시도 했지만 해결책을 찾지 못했다. <br>
```
Environment health has transitioned from Ok to Severe. 75.0 % of the requests are failing with HTTP 5xx.
```

* 일단 여기까지 학습한 자신을 위로하며, 책을 다시 읽어보고 aws, 무중단 배포에 대해 정리해보고 새 프로젝트를 만들고자 한다. 다시 화이팅,,,
