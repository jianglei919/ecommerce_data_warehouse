-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: ecommerce_source_web
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
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NOT NULL COMMENT 'è®¢å•å· (è€Œéž order_id)',
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_no`) REFERENCES `orders` (`order_no`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,'WEB-2024-001',1,1,899.99,899.99),(2,'WEB-2024-001',6,1,29.99,29.99),(3,'WEB-2024-001',7,4,19.99,79.96),(4,'WEB-2024-002',2,1,649.99,649.99),(5,'WEB-2024-003',1,2,899.99,1799.98),(6,'WEB-2024-003',6,1,29.99,29.99),(7,'WEB-2024-003',8,9,7.99,71.91),(8,'WEB-2024-003',9,1,24.99,24.99),(9,'WEB-2024-004',3,1,229.99,229.99),(10,'WEB-2024-004',10,7,12.99,90.93),(11,'WEB-2024-005',4,1,799.99,799.99),(12,'WEB-2024-005',7,1,19.99,19.99),(13,'WEB-2024-005',5,1,299.99,299.99),(14,'WEB-2024-006',2,2,649.99,1299.98),(15,'WEB-2024-006',3,1,229.99,229.99),(16,'WEB-2024-006',6,5,29.99,149.95),(17,'WEB-2024-007',3,1,229.99,229.99),(18,'WEB-2024-008',8,50,7.99,399.50),(19,'WEB-2024-008',10,49,12.99,490.51),(20,'WEB-2024-009',7,2,19.99,39.98),(21,'WEB-2024-009',9,2,24.99,49.98),(22,'WEB-2024-010',2,1,649.99,649.99),(23,'WEB-2024-010',6,1,29.99,29.99),(24,'WEB-2024-010',10,8,12.99,103.92),(25,'WEB-2024-010',7,1,19.99,19.99);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_no` varchar(50) NOT NULL COMMENT 'è®¢å•å· (Web)',
  `user_id` int NOT NULL,
  `order_date` date NOT NULL COMMENT 'Webæ ¼å¼: MM/dd/yyyy (é€»è¾‘)',
  `total_amount` decimal(15,2) NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_no`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES ('WEB-2024-001',1,'2024-01-15',1129.98,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-002',2,'2024-01-16',649.99,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-003',3,'2024-01-17',2099.96,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-004',1,'2024-01-18',299.97,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-005',4,'2024-01-19',529.97,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-006',5,'2024-01-20',1579.94,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-007',2,'2024-01-21',229.99,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-008',3,'2024-01-22',889.96,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-009',1,'2024-01-23',79.96,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28'),('WEB-2024-010',4,'2024-01-24',749.97,'completed','2026-04-04 21:10:28','2026-04-04 21:10:28');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_reviews`
--

DROP TABLE IF EXISTS `product_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_reviews` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `user_id` int NOT NULL,
  `rating` int NOT NULL,
  `review_text` text,
  `review_date` date NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_review_date` (`review_date`),
  CONSTRAINT `product_reviews_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `product_reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `product_reviews_chk_1` CHECK (((`rating` >= 1) and (`rating` <= 5)))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_reviews`
--

LOCK TABLES `product_reviews` WRITE;
/*!40000 ALTER TABLE `product_reviews` DISABLE KEYS */;
INSERT INTO `product_reviews` VALUES (1,1,1,5,'Great Android phone, amazing display','2024-01-16','2026-04-04 21:10:28'),(2,1,2,5,'Best Samsung phone so far','2024-01-17','2026-04-04 21:10:28'),(3,2,3,4,'Excellent tablet for productivity','2024-01-18','2026-04-04 21:10:28'),(4,4,4,5,'Pixel 8 is fantastic','2024-01-19','2026-04-04 21:10:28'),(5,3,5,4,'Good wireless earbuds','2024-01-20','2026-04-04 21:10:28'),(6,5,1,5,'Love the Pixel Watch','2024-01-21','2026-04-04 21:10:28'),(7,2,2,5,'Samsung Galaxy Tab is perfect','2024-01-22','2026-04-04 21:10:28'),(8,4,3,5,'Excellent performance','2024-01-23','2026-04-04 21:10:28'),(9,3,4,4,'Great sound quality','2024-01-24','2026-04-04 21:10:28'),(10,1,5,4,'Very recommended phone','2024-01-25','2026-04-04 21:10:28');
/*!40000 ALTER TABLE `product_reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(200) NOT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  KEY `idx_category` (`category`),
  KEY `idx_product_name` (`product_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Samsung Galaxy S24','Electronics',899.99,55,'2026-04-04 21:10:28'),(2,'Samsung Galaxy Tab S9','Electronics',649.99,40,'2026-04-04 21:10:28'),(3,'Samsung Galaxy Buds Pro','Audio',229.99,90,'2026-04-04 21:10:28'),(4,'Google Pixel 8','Electronics',799.99,50,'2026-04-04 21:10:28'),(5,'Google Pixel Watch','Wearables',299.99,75,'2026-04-04 21:10:28'),(6,'USB Charger','Accessories',29.99,400,'2026-04-04 21:10:28'),(7,'Case Cover','Accessories',19.99,250,'2026-04-04 21:10:28'),(8,'Screen Film','Accessories',7.99,350,'2026-04-04 21:10:28'),(9,'Phone Mount','Accessories',24.99,120,'2026-04-04 21:10:28'),(10,'Tempered Glass','Accessories',12.99,280,'2026-04-04 21:10:28');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'user_web_001','user1@web.com','2026-04-04 21:10:28'),(2,'user_web_002','user2@web.com','2026-04-04 21:10:28'),(3,'user_web_003','user3@web.com','2026-04-04 21:10:28'),(4,'user_web_004','user4@web.com','2026-04-04 21:10:28'),(5,'user_web_005','user5@web.com','2026-04-04 21:10:28');
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

-- Dump completed on 2026-04-04 21:42:54
