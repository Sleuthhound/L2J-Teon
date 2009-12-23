-- ----------------------------
-- Table structure for auto_announcements
-- ----------------------------
CREATE TABLE `auto_announcements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `announcement` varchar(255) COLLATE latin1_general_ci NOT NULL DEFAULT '',
  `delay` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;