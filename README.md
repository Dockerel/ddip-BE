<h1 align="center">띱</h1>

## 개요
캠퍼스에서 위치 기반 주변 학생들과 실시간 정보를 거래할 수 있는 서비스

## 기술 스택
<p dir="auto">
<img src="https://img.shields.io/badge/-Spring-6DB33F?logo=Spring&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-SpringBoot-6DB33F?logo=springboot&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-SpringDataJpa-6DB33F?logo=SpringDataJpa&amp;logoColor=white&amp;labelColor=6DB33F" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-Junit5-25A162?logo=Junit5&amp;logoColor=white&amp;labelColor=25A162" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-MySql-438CB2?logo=MySql&amp;logoColor=white&amp;labelColor=438CB2" style="max-width: 100%;">
<img src="https://img.shields.io/badge/-Redis-FF4438?logo=Redis&amp;logoColor=white&amp;labelColor=FF4438" style="max-width: 100%;">
<br>
<img src="https://img.shields.io/badge/-RabbitMQ-FF6600?logo=RabbitMQ&amp;logoColor=white&amp;labelColor=FF6600" style="max-width: 100%;">
</p>

## 개발 정보
* 2024.09 - 2024.11 (3M / 6명) [BE, AI]

## 시스템 아키텍처
<img width="954" height="472" alt="image" src="https://github.com/user-attachments/assets/274ad5a9-a1ce-45da-9809-8e42ae274eaa" />

## 담당한 기능
* [위치 기반 유저 관리](#1-위치-기반-유저-관리)
* [위치 기반 요청 조회](#2-위치-기반-요청-조회)
* [실시간 유저 수 집계](#3-실시간-유저-수-집계)

## 1. 위치 기반 유저 관리
### 개요
<img width="444" height="448" alt="image" src="https://github.com/user-attachments/assets/a944e162-5c7b-46de-8733-ab88a6d59322" />

* 유저의 위치를 셀로 관리하기 위해 S2 라이브러리를 사용
* 정기적으로 사용자의 스마트폰에서 위치 정보를 전송하여 어느 셀에 유저가 존재하는지 데이터베이스에 저장
* 주변에 있는 요청들을 찾아줄 때 사용
* 셀에 존재하는 유저의 조회와 삭제가 빠르게 이루어지게 하기 위해 레디스 사용

### 문제 및 의사결정 과정
#### 1. 유저 위치 저장 방식 결정 : Key-Value 방식 vs Set + Key-Value + ZSet 방식
**Key-Value 방식**
* TTL 설정으로 만료된 유저 자동 삭제와 단일 유저의 조회는 빠르게 이루어짐
* 특정 셀에 존재하는 유저 조회 시 O(N) 시간으로 부적합

**Set + Key-Value + ZSet 방식**
* 복합 방식으로 특정 셀에 존재하는 유저를 Set에 저장하여 빠르게 조회 가능
* 유저의 위치가 바뀌었을 때 해당 유저가 이전에 존재했던 위치를 저장하기 위해 Key-Value 사용
* 일정 시간마다 ZSet을 사용하여 만료된 유저 삭제

> **Set + Key-Value + ZSet 복합 방식 선택**

#### 2. 유저 위치 관리 동시성 문제 고려 : synchronized vs 분산락 vs Lua Script
* 유저 위치 업데이트 요청이 동시에 들어오면 동시성 문제가 발생할 수 있음
* 기존 위치 업데이트 방식
  1. 이전 유저 위치 정보 조회
  2. 이전 유저 위치 정보 삭제
  3. 현재 위치 저장
<img width="445" height="429" alt="image" src="https://github.com/user-attachments/assets/872ebf00-b633-4ace-b1d0-22502fa9520f" />

* 기존 위치 업데이트 방식은 3단계의 데이터 갱신이 원자적으로 이루어지지 않아 유저가 2개 이상의 기기에서 위치 업데이트 시 동시성 문제가 발생할 수 있음
* 동시에 2개 이상의 위치에 존재하게 되는 문제가 발생할 수 있음
* 다중 인스턴스 환경이므로 서버 스케일 아웃 시 동시성 보장 필요

**synchronized**
* JVM 레벨 락으로 다중 인스턴스 환경에서 동시성 제어 불가

**분산락**
* 인스턴스 간 동시성 보장하나 락 획득/대기/해제 오버헤드로 성능 저하

**Lua Script**
* Redis 싱글스레드 특성으로 원자성 보장 및 네트워크 왕복 최소화로 빠른 성능 보장

> [**Lua Script 선택**](https://github.com/Dockerel/ddip-BE/blob/main/src/main/resources/luascript/save_user_location.lua)

### 성과
* Lua Script 도입으로 synchronized 대비 위치 갱신 평균 응답시간 5배 개선
* 1s &rarr; 191ms, 동시 위치 갱신 1,000건 기준


## 2. 위치 기반 요청 조회
### 개요
* 유저 위치 기준 요청 거리순 정렬을 위해 비효율적인 유클리디안 거리 방식을 사용하고 있었음
### 문제 및 의사결정 과정
#### 1. 하버사인 방식 및 공간 인덱싱 도입
**하버사인 방식**
* 요청 수가 늘어났을 때를 대비하여 더욱 정확한 거리수 정렬을 위해 위도, 경도 기반으로 거리를 계산하는 방식인 하버사인 방식 도입

**공간 인덱싱**
* 지리적 정보를 효율적으로 검색하기 위해, 공간 데이터에 적용시키는 인덱스인 공간 인덱스 도입
```java
@Query(value = """
                SELECT * FROM ddip_event
                WHERE ST_CONTAINS(ST_Buffer(ST_SRID(POINT(:lng, :lat), 4326), :dist), local_point)
                ORDER BY ST_Distance_Sphere(ST_SRID(POINT(:lng, :lat), 4326), local_point)
            """, nativeQuery = true)
    List<DdipEventEntity> findAllByDistance(@Param("lng") Double lng, @Param("lat") Double lat, @Param("dist") Double dist);
```

* 유저의 위치를 기준으로 해당 영역이 지정한 범위내에 포함되는지 확인하는 방식
<img width="476" height="333" alt="image" src="https://github.com/user-attachments/assets/c6b696cd-77d1-420e-bc1f-e504198703b3" />

* 유저의 스마트폰 화면의 모서리와 현재 사용자 위치 기준 가장 긴 거리 기반으로 해당 거리를 반지름, 유저의 좌표를 중심으로 하는 원을 만들어 해당 반경 내에 요청들을 가져오는 방식

### 성과
* 공간 인덱스를 도입하여 기존 유클리디안 방식 대비 조회 시간 10배 개선
* 1,600ms &rarr; 160ms, 랜덤 위치 요청 1,000건 기준

## 3. 실시간 유저 수 집계
### 개요
* 실시간 유저 수 집계를 위해 레디스에 셀별 개별 요청을 반복적으로 넣어 전체 사용자 수를 집계
* 많은 호출과 부하로 인해 응답 지연, 서버 과부하 문제 발생

### 문제 및 의사결정 과정
#### 2. 펜윅트리와 RabbitMQ 도입
**펜윅트리**
* 펜윅트리(Fenwick Tree)는 최하위 켜져있는 비트를 기반으로 트리를 만들어 동적배열에서 구간 합을 효율적으로 구할 수 있는 자료구조
* [FenwickTree.java](https://github.com/Dockerel/ddip-BE/blob/main/src/main/java/com/knu/ddip/location/business/util/FenwickTree.java)
* [CellStatusService.java](https://github.com/Dockerel/ddip-BE/blob/main/src/main/java/com/knu/ddip/location/business/service/CellStatusService.java)

**RabbitMQ**
* RabbitMQ 메시지큐 도입으로 동시성 문제 방지
* 트래픽 조절 및 부하 분산 가능

### 성과
* 기존 전체 집계 성능 대비 84배 개선
* 105,788,709ns &rarr; 1,262,875ns, 유저 위치 10,000건 기준
* 유저 위치가 10배로 증가해도, 기존 전체 집계 성능 대비 47배 개선
* 105,788,709ns &rarr; 2,256,458ns, 유저 위치 100,000건 기준
