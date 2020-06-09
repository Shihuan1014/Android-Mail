/*
Navicat MySQL Data Transfer

Source Server         : 数据库系统实验
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : email

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-06-09 12:05:45
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for attachment
-- ----------------------------
DROP TABLE IF EXISTS `attachment`;
CREATE TABLE `attachment` (
  `userAddress` varchar(40) DEFAULT NULL,
  `mailBox` varchar(50) DEFAULT NULL,
  `path` varchar(50) DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `name` varchar(30) DEFAULT NULL,
  `uid` int(11) NOT NULL,
  PRIMARY KEY (`uid`),
  CONSTRAINT `FK_Relationship_4` FOREIGN KEY (`uid`) REFERENCES `mail` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of attachment
-- ----------------------------

-- ----------------------------
-- Table structure for block
-- ----------------------------
DROP TABLE IF EXISTS `block`;
CREATE TABLE `block` (
  `userAddress` varchar(30) NOT NULL,
  `blockAddress` varchar(30) NOT NULL,
  PRIMARY KEY (`userAddress`,`blockAddress`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of block
-- ----------------------------
INSERT INTO `block` VALUES ('shihuan@test.com', 'ad@test.com');
INSERT INTO `block` VALUES ('shihuan@test.com', 'guanggao@test.com');

-- ----------------------------
-- Table structure for friend
-- ----------------------------
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `f_id` int(11) NOT NULL AUTO_INCREMENT,
  `id` int(11) DEFAULT NULL,
  `friend_id` int(11) NOT NULL,
  PRIMARY KEY (`f_id`),
  KEY `FK_Relationship_3` (`id`),
  CONSTRAINT `FK_Relationship_3` FOREIGN KEY (`id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of friend
-- ----------------------------

-- ----------------------------
-- Table structure for log
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `type` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `log` varchar(1024) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of log
-- ----------------------------
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:37:44', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:37:44', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:40:09', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:40:09', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:43:48', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:43:48', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:48:53', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:48:54', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:49:15', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:49:15', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:49:40', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 09:49:41', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 21:57:52', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 21:57:52', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 21:59:14', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 21:59:14', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:10:20', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:12:55', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:12:55', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:13:07', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:13:57', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:16:50', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:16:50', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:17:12', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:19:26', 'admin@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:19:38', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:19:38', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:21:41', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:21:41', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:22:03', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:25:18', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:26:58', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:27:54', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:09', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:14', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:22', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:22', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:56', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:28:57', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:31:14', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:31:14', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:35:05', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:35:05', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:38:37', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:38:37', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:42:33', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:43:32', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:43:32', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:43:34', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:43:53', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:44:09', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 22:44:09', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:41:58', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:41:58', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:58:22', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:58:22', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:58:49', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:59:06', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-27 23:59:06', 'guanggao@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:01:27', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:01:27', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:01:27', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:02:49', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:02:49', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:02:49', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:03:23', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:03:23', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:03:23', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:04:36', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:04:37', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:04:37', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:06:56', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:06:56', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:06:56', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:11:28', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:11:28', 'guanggao@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:14:27', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:14:27', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:14:28', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:17:20', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:17:20', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:17:20', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:18:36', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:18:37', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:18:37', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:20:37', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:20:48', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:20:58', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:20:58', 'guanggao@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:38:18', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:38:18', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:38:18', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:40:16', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:40:16', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:40:16', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:43:14', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:43:14', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:43:14', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:44:01', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:44:01', 'guanggao@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:44:01', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:44:59', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:45:10', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:45:15', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:45:29', 'guanggao@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:45:29', 'guanggao@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:50:40', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 00:50:40', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 15:17:30', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 15:17:30', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:12:07', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:12:07', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:19:07', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:19:07', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:19:07', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:21:31', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:21:31', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:23:27', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:23:50', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:24:58', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:24:58', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:25:47', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:25:48', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:27:04', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:27:04', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:29:34', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:29:34', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:30:32', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:30:33', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:31:21', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:31:21', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:31:22', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:33:27', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:33:27', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:34:31', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:37:24', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:37:24', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:37:43', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:37:44', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:42:04', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:42:04', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Server Status', '2020-05-28 18:42:43', 'ShutDown SMTP Server');
INSERT INTO `log` VALUES ('Server Status', '2020-05-28 18:43:01', 'Start SMTP Server');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:47:36', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:48:10', 'ad@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:48:10', 'ad@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:48:56', 'ad@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 18:53:21', 'ad@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:26:28', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:26:28', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:26:28', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:26:38', 'admin@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:29:03', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:29:03', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:29:04', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:37:17', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:37:17', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:37:19', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:38:19', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:38:20', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:38:55', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:54:15', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:54:15', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:54:18', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:55:21', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:55:22', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:55:54', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:59:00', 'admin@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:59:01', 'admin@test.comsend an email');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:59:04', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 19:59:05', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:00:08', 'shihuan@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:00:08', 'shihuan@test.comsend an email');
INSERT INTO `log` VALUES ('Server Status', '2020-05-28 20:01:19', 'ShutDown SMTP Server');
INSERT INTO `log` VALUES ('Server Status', '2020-05-28 20:01:29', 'Start SMTP Server');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:04:10', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:04:25', 'ad@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:05:38', 'ad@test.comlogin SmtpServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:13:13', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:13:13', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:13:14', 'admin@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:55:03', 'shihuan@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:55:03', 'ad@test.com\nlogin PopServer success');
INSERT INTO `log` VALUES ('Email', '2020-05-28 20:55:04', 'admin@test.com\nlogin PopServer success');

-- ----------------------------
-- Table structure for mail
-- ----------------------------
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `uid` int(11) NOT NULL,
  `messagId` varchar(30) DEFAULT NULL,
  `userAddress` varchar(40) DEFAULT NULL,
  `avatar` varchar(40) DEFAULT NULL,
  `from` varchar(40) DEFAULT NULL,
  `fromEmail` varchar(30) DEFAULT NULL,
  `to` varchar(30) DEFAULT NULL,
  `toEmail` varchar(30) DEFAULT NULL,
  `sendTime` date DEFAULT NULL,
  `subject` varchar(50) DEFAULT NULL,
  `data` text,
  `flag` int(11) DEFAULT NULL,
  `deleted` int(11) DEFAULT NULL,
  `draft` int(11) DEFAULT NULL,
  `seen` int(11) DEFAULT NULL,
  `mailBox` varchar(50) DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `textContent` varchar(50) DEFAULT NULL,
  `replyTo` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  CONSTRAINT `FK_Relationship_1` FOREIGN KEY (`uid`) REFERENCES `mailbox` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of mail
-- ----------------------------

-- ----------------------------
-- Table structure for mailbox
-- ----------------------------
DROP TABLE IF EXISTS `mailbox`;
CREATE TABLE `mailbox` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `id` int(11) DEFAULT NULL,
  `from` varchar(40) NOT NULL,
  `to` varchar(30) NOT NULL,
  `date` datetime DEFAULT NULL,
  `data` longtext,
  `size` int(20) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  KEY `FK_Relationship_2` (`id`),
  CONSTRAINT `FK_Relationship_2` FOREIGN KEY (`id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1130 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of mailbox
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(30) NOT NULL,
  `password` varchar(30) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `nickName` varchar(20) DEFAULT NULL,
  `avatarUrl` varchar(60) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `author` int(11) NOT NULL DEFAULT '0',
  `type` int(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1010 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1003', 'admin@test.com', '123456', null, null, null, null, '0', '1');
INSERT INTO `user` VALUES ('1007', 'shihuan@test.com', '1234567', null, 'shihuan@test.com', null, null, '0', '0');
INSERT INTO `user` VALUES ('1009', 'ad@test.com', '123456', null, 'ad@test.com', null, null, '-1', '0');
