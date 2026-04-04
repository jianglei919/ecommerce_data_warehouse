-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: ecommerce_source_app
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
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1001,1,1,999.99,999.99),(2,1001,6,1,19.99,19.99),(3,1001,8,5,29.99,149.95),(4,1002,4,1,599.99,599.99),(5,1003,2,1,1999.99,1999.99),(6,1003,6,1,19.99,19.99),(7,1003,7,4,79.99,319.96),(8,1004,3,1,249.99,249.99),(9,1004,9,8,9.99,79.99),(10,1005,5,1,399.99,399.99),(11,1005,8,1,29.99,49.98),(12,1006,2,1,1999.99,1999.99),(13,1006,1,1,999.99,999.99),(14,1006,10,1,49.99,49.99),(15,1007,3,1,249.99,249.99),(16,1008,7,5,79.99,399.95),(17,1008,9,41,9.99,409.59),(18,1009,8,3,29.99,89.97),(19,1009,6,1,9.99,9.99),(20,1010,4,1,599.99,599.99),(21,1010,6,1,19.99,19.99),(22,1010,10,3,49.99,149.97);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT 'è®¢å•ID (App)',
  `user_id` int NOT NULL,
  `order_date` date NOT NULL COMMENT 'Appæ ¼å¼: yyyy-MM-dd',
  `total_amount` decimal(15,2) NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1011 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1001,1,'2024-01-15',1249.98,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1002,2,'2024-01-16',599.99,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1003,3,'2024-01-17',2299.97,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1004,1,'2024-01-18',329.98,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1005,4,'2024-01-19',449.97,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1006,5,'2024-01-20',1569.95,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1007,2,'2024-01-21',249.99,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1008,3,'2024-01-22',809.96,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1009,1,'2024-01-23',99.97,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30'),(1010,4,'2024-01-24',649.98,'completed','2026-04-04 19:48:30','2026-04-04 19:48:30');
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
INSERT INTO `product_reviews` VALUES (1,1,1,5,'Excellent phone, great camera!','2024-01-16','2026-04-04 19:48:30'),(2,1,2,5,'Best iPhone I ever had','2024-01-17','2026-04-04 19:48:30'),(3,4,3,4,'Good tablet, very fast','2024-01-18','2026-04-04 19:48:30'),(4,2,4,5,'Powerful laptop, highly recommended','2024-01-19','2026-04-04 19:48:30'),(5,3,5,4,'Great sound quality','2024-01-20','2026-04-04 19:48:30'),(6,2,1,5,'Worth every penny','2024-01-21','2026-04-04 19:48:30'),(7,4,2,5,'Perfect for work and entertainment','2024-01-22','2026-04-04 19:48:30'),(8,5,3,4,'Good watch, battery lasts long','2024-01-23','2026-04-04 19:48:30'),(9,3,4,5,'Best earbuds ever','2024-01-24','2026-04-04 19:48:30'),(10,7,5,4,'Fast charging, very convenient','2024-01-25','2026-04-04 19:48:30');
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
INSERT INTO `products` VALUES (1,'iPhone 15 Pro','Electronics',999.99,50,'2026-04-04 19:48:30'),(2,'MacBook Pro M3','Electronics',1999.99,30,'2026-04-04 19:48:30'),(3,'AirPods Pro','Audio',249.99,100,'2026-04-04 19:48:30'),(4,'iPad Air','Electronics',599.99,45,'2026-04-04 19:48:30'),(5,'Apple Watch','Wearables',399.99,60,'2026-04-04 19:48:30'),(6,'USB-C Cable','Accessories',19.99,500,'2026-04-04 19:48:30'),(7,'Wireless Charger','Accessories',79.99,80,'2026-04-04 19:48:30'),(8,'Phone Case','Accessories',29.99,200,'2026-04-04 19:48:30'),(9,'Screen Protector','Accessories',9.99,300,'2026-04-04 19:48:30'),(10,'Laptop Stand','Accessories',49.99,70,'2026-04-04 19:48:30');
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
INSERT INTO `users` VALUES (1,'user_app_001','user1@app.com','2026-04-04 19:48:30'),(2,'user_app_002','user2@app.com','2026-04-04 19:48:30'),(3,'user_app_003','user3@app.com','2026-04-04 19:48:30'),(4,'user_app_004','user4@app.com','2026-04-04 19:48:30'),(5,'user_app_005','user5@app.com','2026-04-04 19:48:30');
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
