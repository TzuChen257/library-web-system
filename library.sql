CREATE DATABASE  IF NOT EXISTS `library_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `library_system`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: library_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `book_categories`
--

DROP TABLE IF EXISTS `book_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_categories` (
  `category_id` varchar(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文圖書分類法代碼，例如 000、100、900',
  `category_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分類名稱',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分類說明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_book_categories_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍分類表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_categories`
--

LOCK TABLES `book_categories` WRITE;
/*!40000 ALTER TABLE `book_categories` DISABLE KEYS */;
INSERT INTO `book_categories` VALUES ('000','總類','總論、圖書館學、百科全書、普通叢書等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('100','哲學類','哲學、心理學、倫理學等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('200','宗教類','宗教總論、佛教、基督教、其他宗教等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('300','自然科學類','數學、物理、化學、生物、地球科學等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('400','應用科學類','醫藥、工程、農業、家政、商業技術等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('500','社會科學類','教育、法律、政治、經濟、社會學等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('600','史地類','中國史地、傳記、地理等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('700','世界史地類','世界各國歷史與地理等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('800','語文類','語言學、文學、作文、各國文學等','2026-04-28 22:18:46','2026-04-28 22:18:46'),('900','藝術類','音樂、美術、設計、戲劇、電影、體育等','2026-04-28 22:18:46','2026-04-28 22:18:46');
/*!40000 ALTER TABLE `book_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_copies`
--

DROP TABLE IF EXISTS `book_copies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_copies` (
  `copy_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '館藏複本ID，由後端IdGenerator產生，例如 CP00000001',
  `book_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '對應書目主檔',
  `copy_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '實體館藏條碼或館藏編號，例如 B00000001',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '館藏位置，例如 A區-01櫃',
  `copy_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE=可借閱, BORROWED=借出中, RETURN_PENDING=歸還待審核, RESERVED=已保留, DAMAGED=毀損, LOST=遺失',
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '館藏備註',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`copy_id`),
  UNIQUE KEY `copy_code` (`copy_code`),
  KEY `idx_book_copies_book_id` (`book_id`),
  KEY `idx_book_copies_status` (`copy_status`),
  CONSTRAINT `fk_book_copies_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍複本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_copies`
--

LOCK TABLES `book_copies` WRITE;
/*!40000 ALTER TABLE `book_copies` DISABLE KEYS */;
INSERT INTO `book_copies` VALUES ('CP00000001','BK00000001','B00000001','A區-01櫃','AVAILABLE',NULL,'2026-04-28 22:18:46','2026-04-28 22:18:46'),('CP00000002','BK00000001','B00000002','A區-01櫃','AVAILABLE',NULL,'2026-04-28 22:18:46','2026-04-28 22:18:46'),('CP00000003','BK00000002','B00000003','A區-02櫃','AVAILABLE',NULL,'2026-04-28 22:18:46','2026-04-28 22:18:46'),('CP00000004','BK00000002','B00000004','A區-02櫃','AVAILABLE',NULL,'2026-04-28 22:18:46','2026-04-28 22:18:46'),('CP00000005','BK00000003','B00000005','B區-01櫃','AVAILABLE',NULL,'2026-04-28 22:18:46','2026-04-28 22:18:46');
/*!40000 ALTER TABLE `book_copies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `book_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '書目ID，由後端IdGenerator產生，例如 BK00000001',
  `category_id` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '中文圖書分類法代碼',
  `isbn` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ISBN',
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '書名',
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
  `publisher` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出版社',
  `publish_year` int DEFAULT NULL COMMENT '出版年份',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '書籍簡介',
  `cover_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面圖片網址',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE=上架, DISABLED=下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_id`),
  KEY `idx_books_title` (`title`),
  KEY `idx_books_author` (`author`),
  KEY `idx_books_isbn` (`isbn`),
  KEY `idx_books_category_id` (`category_id`),
  KEY `idx_books_status` (`status`),
  CONSTRAINT `fk_books_category` FOREIGN KEY (`category_id`) REFERENCES `book_categories` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書目主檔';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES ('BK00000001','400','9789865020001','Java 程式設計入門','王小明','範例出版社',2023,'Java 基礎語法與物件導向入門書籍',NULL,'ACTIVE','2026-04-28 22:18:46','2026-04-28 22:18:46'),('BK00000002','400','9789865020002','Spring Boot 後端開發實務','陳大華','範例出版社',2024,'Spring Boot、REST API 與資料庫整合實務',NULL,'ACTIVE','2026-04-28 22:18:46','2026-04-28 22:18:46'),('BK00000003','800','9789865020003','小王子','Antoine de Saint-Exupéry','範例出版社',2020,'經典文學作品',NULL,'ACTIVE','2026-04-28 22:18:46','2026-04-28 22:18:46');
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow_records`
--

DROP TABLE IF EXISTS `borrow_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `borrow_records` (
  `borrow_id` bigint NOT NULL AUTO_INCREMENT COMMENT '借閱紀錄ID，自動產生',
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '借閱者',
  `copy_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '實際借出的館藏複本',
  `borrow_date` date NOT NULL COMMENT '借閱日期',
  `due_date` date NOT NULL COMMENT '應還日期',
  `return_request_date` date DEFAULT NULL COMMENT '讀者送出歸還申請日期',
  `actual_return_date` date DEFAULT NULL COMMENT '管理員審核通過後的實際歸還日期',
  `borrow_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BORROWED' COMMENT 'BORROWED=借閱中, RETURN_PENDING=歸還待審核, RETURNED=已歸還, OVERDUE=逾期, LOST=遺失',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`borrow_id`),
  KEY `idx_borrow_records_user_id` (`user_id`),
  KEY `idx_borrow_records_copy_id` (`copy_id`),
  KEY `idx_borrow_records_status` (`borrow_status`),
  KEY `idx_borrow_records_due_date` (`due_date`),
  CONSTRAINT `fk_borrow_records_copy` FOREIGN KEY (`copy_id`) REFERENCES `book_copies` (`copy_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_borrow_records_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借閱紀錄表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_records`
--

LOCK TABLES `borrow_records` WRITE;
/*!40000 ALTER TABLE `borrow_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `borrow_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '訊息ID，自動產生',
  `receiver_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接收者，對應 users.user_id',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '訊息標題',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '訊息內容',
  `message_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL' COMMENT 'GENERAL=一般訊息, BORROW=借閱通知, RETURN=歸還通知, RESERVATION=預約通知, OVERDUE=逾期通知',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0=未讀, 1=已讀',
  `related_borrow_id` bigint DEFAULT NULL COMMENT '關聯借閱紀錄，可為 NULL',
  `related_reservation_id` bigint DEFAULT NULL COMMENT '關聯預約紀錄，可為 NULL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `read_at` datetime DEFAULT NULL COMMENT '讀取時間',
  PRIMARY KEY (`message_id`),
  KEY `idx_messages_receiver_id` (`receiver_id`),
  KEY `idx_messages_is_read` (`is_read`),
  KEY `idx_messages_type` (`message_type`),
  KEY `idx_messages_created_at` (`created_at`),
  KEY `idx_messages_related_borrow_id` (`related_borrow_id`),
  KEY `idx_messages_related_reservation_id` (`related_reservation_id`),
  CONSTRAINT `fk_messages_borrow` FOREIGN KEY (`related_borrow_id`) REFERENCES `borrow_records` (`borrow_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_messages_reservation` FOREIGN KEY (`related_reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訊息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '預約紀錄ID，自動產生',
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '預約者',
  `book_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '預約書目',
  `reservation_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '預約時間',
  `expire_date` datetime DEFAULT NULL COMMENT '保留到期時間',
  `reservation_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING=等待中, AVAILABLE_NOTICE=已通知可借, COMPLETED=已完成借閱, CANCELLED=已取消, EXPIRED=已過期',
  `queue_order` int DEFAULT NULL COMMENT '預約順位，可由後端計算或寫入',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`reservation_id`),
  KEY `idx_reservations_user_id` (`user_id`),
  KEY `idx_reservations_book_id` (`book_id`),
  KEY `idx_reservations_status` (`reservation_status`),
  KEY `idx_reservations_date` (`reservation_date`),
  CONSTRAINT `fk_reservations_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reservations_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='預約紀錄表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用者ID，由後端IdGenerator產生，例如 U00000001',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登入帳號',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密碼，後端應儲存加密後結果',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用者姓名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '電子信箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '聯絡電話',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'READER' COMMENT 'READER=讀者, ADMIN=管理員',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE=啟用, DISABLED=停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_username` (`username`),
  KEY `idx_users_role` (`role`),
  KEY `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('U00000001','admin','admin123','系統管理員','admin@example.com',NULL,'ADMIN','ACTIVE','2026-04-28 22:18:46','2026-04-28 22:18:46'),('U00000002','reader01','reader123','測試讀者','reader01@example.com',NULL,'READER','ACTIVE','2026-04-28 22:18:46','2026-04-28 22:18:46');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-29  7:32:33
