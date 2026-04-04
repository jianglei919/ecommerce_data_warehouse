-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: ecommerce_warehouse
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dim_order_items`
--

DROP TABLE IF EXISTS `dim_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dim_order_items` (
  `item_id` int NOT NULL AUTO_INCREMENT COMMENT 'è®¢å•é¡¹ID',
  `order_id` int NOT NULL,
  `product_id` int NOT NULL COMMENT 'å•†å“ID',
  `product_name` varchar(200) NOT NULL COMMENT 'å•†å“åç§°',
  `category` varchar(50) DEFAULT NULL COMMENT 'å•†å“ç±»åˆ«',
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `dim_order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `dim_orders` (`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è®¢å•é¡¹ç»´åº¦è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dim_order_items`
--

LOCK TABLES `dim_order_items` WRITE;
/*!40000 ALTER TABLE `dim_order_items` DISABLE KEYS */;
INSERT INTO `dim_order_items` VALUES (1,1,1,'iPhone 15 Pro','Electronics',1,999.99,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(2,1,8,'AirPods Pro Max','Electronics',1,599.99,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(3,2,2,'MacBook Pro M3','Computers',1,1999.99,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(4,3,1,'iPhone 15 Pro','Electronics',1,999.99,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(5,3,4,'iPad Air','Electronics',1,599.99,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(6,4,1,'iPhone 15 Pro','Electronics',1,999.99,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(7,5,2,'MacBook Pro M3','Computers',1,1999.99,1299.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(8,6,3,'Samsung Galaxy S24','Electronics',1,899.99,749.97,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(9,7,4,'iPad Air','Electronics',2,599.99,1199.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(10,8,5,'Dell XPS 15','Computers',1,1799.99,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(11,8,6,'Wireless Mouse','Accessories',1,79.99,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(12,8,7,'USB-C Cable','Accessories',2,19.99,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(13,9,8,'AirPods Pro Max','Electronics',1,599.99,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(14,10,9,'Apple Watch Ultra','Electronics',1,799.99,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(15,10,10,'Phone Charger','Accessories',1,49.99,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(16,10,11,'Screen Protector','Accessories',5,5.99,29.95,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(17,11,12,'Pixel 8 Pro','Electronics',1,899.99,899.97,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(18,12,13,'Sony WH-1000XM5','Electronics',1,399.99,399.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(19,12,14,'Anker Power Bank','Electronics',1,49.99,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(20,12,15,'Laptop Stand','Accessories',1,49.99,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(21,12,16,'Desk Lamp','Accessories',1,99.99,99.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(22,13,17,'Samsung Galaxy Buds','Electronics',1,199.99,199.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(23,13,18,'Case for Phone','Accessories',3,29.99,89.97,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(24,13,19,'Office Chair','Furniture',1,299.99,299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(25,13,20,'Monitor','Electronics',1,399.99,399.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(26,16,3,'Samsung Galaxy S24','Electronics',1,899.99,899.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(27,16,6,'Wireless Mouse','Accessories',1,79.99,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(28,16,7,'USB-C Cable','Accessories',2,19.99,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(29,17,10,'Phone Charger','Accessories',1,49.99,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(30,17,11,'Screen Protector','Accessories',4,5.99,23.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(31,18,5,'Dell XPS 15','Computers',1,1799.99,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(32,18,9,'Apple Watch Ultra','Electronics',1,799.99,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(33,19,12,'Pixel 8 Pro','Electronics',1,899.99,899.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(34,20,2,'MacBook Pro M3','Computers',1,1999.99,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(35,20,14,'Anker Power Bank','Electronics',1,49.99,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25');
/*!40000 ALTER TABLE `dim_order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dim_orders`
--

DROP TABLE IF EXISTS `dim_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dim_orders` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT 'è®¢å•ID',
  `source` varchar(10) NOT NULL COMMENT 'APP æˆ– WEB',
  `app_order_id` int DEFAULT NULL COMMENT 'Appç³»ç»Ÿè®¢å•ID',
  `web_order_no` varchar(50) DEFAULT NULL COMMENT 'Webç³»ç»Ÿè®¢å•å·',
  `user_id` int NOT NULL COMMENT 'ç”¨æˆ·ID',
  `order_date` date NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_source_order` (`source`,`app_order_id`,`web_order_no`),
  KEY `idx_source` (`source`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_source_date` (`source`,`order_date`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è®¢å•ç»´åº¦è¡¨ (App+Web)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dim_orders`
--

LOCK TABLES `dim_orders` WRITE;
/*!40000 ALTER TABLE `dim_orders` DISABLE KEYS */;
INSERT INTO `dim_orders` VALUES (1,'APP',1001,NULL,100,'2024-01-15',2899.97,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(2,'APP',1002,NULL,101,'2024-01-16',599.99,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(3,'APP',1003,NULL,102,'2024-01-17',4099.93,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(4,'APP',1004,NULL,103,'2024-01-18',1599.98,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(5,'APP',1005,NULL,104,'2024-01-19',1599.98,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(6,'WEB',NULL,'WEB-2024-001',105,'2024-02-01',2799.97,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(7,'WEB',NULL,'WEB-2024-002',106,'2024-02-02',3299.96,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(8,'WEB',NULL,'WEB-2024-003',107,'2024-02-03',1649.96,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(9,'WEB',NULL,'WEB-2024-004',108,'2024-02-04',2799.94,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(10,'WEB',NULL,'WEB-2024-005',109,'2024-02-05',3699.93,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(11,'WEB',NULL,'WEB-2024-006',110,'2024-02-06',1199.97,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(12,'WEB',NULL,'WEB-2024-007',111,'2024-02-07',1349.94,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(13,'WEB',NULL,'WEB-2024-008',112,'2024-02-08',1299.95,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(14,'WEB',NULL,'WEB-2024-009',113,'2024-02-09',2249.97,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(15,'WEB',NULL,'WEB-2024-010',114,'2024-02-10',3799.92,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(16,'APP',1006,NULL,105,'2024-01-20',1569.95,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(17,'APP',1007,NULL,102,'2024-01-21',249.99,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(18,'APP',1008,NULL,103,'2024-01-22',809.96,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(19,'APP',1009,NULL,100,'2024-01-23',99.97,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25'),(20,'APP',1010,NULL,104,'2024-01-24',649.98,'completed','2026-04-04 21:29:25','2026-04-04 21:29:25');
/*!40000 ALTER TABLE `dim_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dim_products`
--

DROP TABLE IF EXISTS `dim_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dim_products` (
  `product_key` int NOT NULL AUTO_INCREMENT COMMENT 'ç»´åº¦ä»£ç†é”® (Business Key: source+product_id)',
  `source` varchar(10) NOT NULL COMMENT 'APP æˆ– WEB ä¸šåŠ¡æº',
  `product_id` int NOT NULL COMMENT 'ä¸šåŠ¡é”® - å•†å“ID',
  `product_name` varchar(200) NOT NULL COMMENT 'å•†å“åç§°',
  `category` varchar(50) NOT NULL COMMENT 'å•†å“ç±»åˆ«',
  `brand` varchar(50) DEFAULT NULL COMMENT 'å“ç‰Œ',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_key`),
  UNIQUE KEY `uk_source_product` (`source`,`product_id`),
  KEY `idx_source` (`source`),
  KEY `idx_category` (`category`),
  KEY `idx_brand` (`brand`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å•†å“ç»´åº¦è¡¨ (Surrogate Key: source+product_id)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dim_products`
--

LOCK TABLES `dim_products` WRITE;
/*!40000 ALTER TABLE `dim_products` DISABLE KEYS */;
INSERT INTO `dim_products` VALUES (1,'APP',1,'iPhone 15 Pro','Electronics','Apple','2026-04-04 21:29:25','2026-04-04 21:29:25'),(2,'APP',2,'MacBook Pro M3','Electronics','Apple','2026-04-04 21:29:25','2026-04-04 21:29:25'),(3,'APP',3,'AirPods Pro','Audio','Apple','2026-04-04 21:29:25','2026-04-04 21:29:25'),(4,'APP',4,'Google Pixel 8','Electronics','Google','2026-04-04 21:29:25','2026-04-04 21:29:25'),(5,'APP',5,'Google Pixel Watch','Wearables','Google','2026-04-04 21:29:25','2026-04-04 21:29:25'),(6,'APP',6,'USB-C Cable','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(7,'APP',7,'Wireless Charger','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(8,'APP',8,'Phone Case','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(9,'APP',9,'Screen Protector','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(10,'APP',10,'Laptop Stand','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(11,'WEB',1,'Samsung Galaxy S24','Electronics','Samsung','2026-04-04 21:29:25','2026-04-04 21:29:25'),(12,'WEB',2,'Samsung Galaxy Tab S9','Electronics','Samsung','2026-04-04 21:29:25','2026-04-04 21:29:25'),(13,'WEB',3,'Samsung Galaxy Buds Pro','Audio','Samsung','2026-04-04 21:29:25','2026-04-04 21:29:25'),(14,'WEB',4,'Google Pixel 8','Electronics','Google','2026-04-04 21:29:25','2026-04-04 21:29:25'),(15,'WEB',5,'Google Pixel Watch','Wearables','Google','2026-04-04 21:29:25','2026-04-04 21:29:25'),(16,'WEB',6,'USB Charger','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(17,'WEB',7,'Case Cover','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(18,'WEB',8,'Screen Film','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(19,'WEB',9,'Phone Mount','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25'),(20,'WEB',10,'Tempered Glass','Accessories','Generic','2026-04-04 21:29:25','2026-04-04 21:29:25');
/*!40000 ALTER TABLE `dim_products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fact_sales_by_product_time`
--

DROP TABLE IF EXISTS `fact_sales_by_product_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fact_sales_by_product_time` (
  `fact_id` int NOT NULL AUTO_INCREMENT COMMENT 'äº‹å®žè¡¨ID',
  `product_key` int NOT NULL COMMENT 'FK: dim_products.product_key',
  `year` int NOT NULL COMMENT 'å¹´ä»½',
  `month` int NOT NULL COMMENT 'æœˆä»½',
  `day` int NOT NULL COMMENT 'æ—¥æœŸ',
  `total_quantity` int NOT NULL COMMENT 'é”€å”®æ•°é‡',
  `total_sales_amount` decimal(15,2) NOT NULL COMMENT 'é”€å”®é‡‘é¢',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`fact_id`),
  UNIQUE KEY `uk_product_time` (`product_key`,`year`,`month`,`day`),
  KEY `idx_product_key` (`product_key`),
  KEY `idx_year_month_day` (`year`,`month`,`day`),
  KEY `idx_date_range` (`year`,`month`),
  CONSTRAINT `fact_sales_by_product_time_ibfk_1` FOREIGN KEY (`product_key`) REFERENCES `dim_products` (`product_key`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é”€å”®äº‹å®žè¡¨ (æŒ‰å•†å“+æ—¶é—´ç»´åº¦)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fact_sales_by_product_time`
--

LOCK TABLES `fact_sales_by_product_time` WRITE;
/*!40000 ALTER TABLE `fact_sales_by_product_time` DISABLE KEYS */;
INSERT INTO `fact_sales_by_product_time` VALUES (1,1,2024,1,15,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(2,1,2024,1,17,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(3,1,2024,1,18,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(4,2,2024,1,16,1,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(5,2,2024,1,19,1,1299.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(6,2,2024,1,24,1,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(7,3,2024,2,1,1,749.97,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(8,3,2024,1,20,1,899.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(9,4,2024,1,17,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(10,4,2024,2,2,2,1199.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(11,5,2024,2,3,1,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(12,5,2024,1,22,1,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(13,6,2024,2,3,1,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(14,6,2024,1,20,1,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(15,7,2024,2,3,2,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(16,7,2024,1,20,2,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(17,8,2024,1,15,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(18,8,2024,2,4,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(19,9,2024,2,5,1,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(20,9,2024,1,22,1,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(21,10,2024,2,5,1,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(22,10,2024,1,21,1,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(23,11,2024,1,15,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(24,11,2024,1,17,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(25,11,2024,1,18,1,999.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(26,12,2024,1,16,1,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(27,12,2024,1,19,1,1299.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(28,12,2024,1,24,1,1299.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(29,13,2024,2,1,1,749.97,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(30,13,2024,1,20,1,899.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(31,14,2024,1,17,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(32,14,2024,2,2,2,1199.98,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(33,15,2024,2,3,1,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(34,15,2024,1,22,1,1799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(35,16,2024,2,3,1,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(36,16,2024,1,20,1,79.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(37,17,2024,2,3,2,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(38,17,2024,1,20,2,39.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(39,18,2024,1,15,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(40,18,2024,2,4,1,599.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(41,19,2024,2,5,1,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(42,19,2024,1,22,1,799.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(43,20,2024,2,5,1,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25'),(44,20,2024,1,21,1,49.99,'2026-04-04 21:29:25','2026-04-04 21:29:25');
/*!40000 ALTER TABLE `fact_sales_by_product_time` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-04 21:42:54
