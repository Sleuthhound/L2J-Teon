-- 
-- Table structure for table `mods_buffer_skills`
-- 

CREATE TABLE IF NOT EXISTS `mods_buffer_skills` (
  id int(10) unsigned NOT NULL default '0',
  level int(10) unsigned NOT NULL default '0',
  skill_group varchar(20) NOT NULL default 'default',
  adena int not null default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM;

-- 
-- Table structure for table `mods_buffer_schemes`
-- 

CREATE TABLE IF NOT EXISTS `mods_buffer_schemes` (
 ownerId int unsigned not null default '0',
 id int(10) unsigned not null default '0',
 level int(10) unsigned NOT NULL default '0',
 scheme varchar(20) NOT NULL default 'default' 
) ENGINE=MyISAM;

INSERT INTO `mods_buffer_skills` (`id`, `level`, `skill_group`, `adena`) VALUES
(264,1,'Songs',0),
(265,1,'Songs',0),
(266,1,'Songs',0),
(267,1,'Songs',0),
(268,1,'Songs',0),
(269,1,'Songs',0),
(270,1,'Songs',0),
(304,1,'Songs',0),
(305,1,'Songs',0),
(306,1,'Songs',0),
(308,1,'Songs',0),
(349,1,'Songs',0),
(363,1,'Songs',0),
(364,1,'Songs',0),
(271,1,'Dances',0),
(272,1,'Dances',0),
(273,1,'Dances',0),
(274,1,'Dances',0),
(275,1,'Dances',0),
(276,1,'Dances',0),
(277,1,'Dances',0),
(309,1,'Dances',0),
(310,1,'Dances',0),
(311,1,'Dances',0),
(530,1,'Dances',0),
(1002,3,'WarCryer',0),
(1006,3,'WarCryer',0),
(1007,3,'WarCryer',0),
(1009,3,'WarCryer',0),
(1308,3,'WarCryer',0),
(1309,3,'WarCryer',0),
(1310,3,'WarCryer',0),
(1362,1,'WarCryer',0),
(1390,3,'WarCryer',0),
(1391,3,'WarCryer',0),
(1413,1,'WarCryer',0),
(1416,1,'OverLord',0),
(1003,3,'OverLord',0),
(1004,3,'OverLord',0),
(1005,3,'OverLord',0),
(1008,3,'OverLord',0),
(1249,3,'OverLord',0),
(1364,1,'OverLord',0),
(1365,1,'OverLord',0),
(1032,3,'Prophet',0),
(1033,3,'Prophet',0),
(1035,4,'Prophet',0),
(1036,2,'Prophet',0),
(1040,3,'Prophet',0),
(1043,1,'Prophet',0),
(1044,3,'Prophet',0),
(1045,6,'Prophet',0),
(1048,6,'Prophet',0),
(1059,3,'ShillenElder',0),
(1062,2,'Prophet',0),
(1068,3,'Prophet',0),
(1073,2,'Elder',0),
(1077,3,'ShillenElder',0),
(1078,6,'ShillenElder',0),
(1085,3,'Prophet',0),
(1086,2,'Prophet',0),
(1087,3,'Elder',0),
(1182,3,'Elder',0),
(1189,3,'ShillenElder',0),
(1191,3,'Prophet',0),
(1204,2,'Prophet',0),
(1242,3,'ShillenElder',0),
(1243,6,'Prophet',0),
(1259,4,'Elder',0),
(1268,4,'ShillenElder',0),
(1303,2,'ShillenElder',0),
(1304,3,'Elder',0),
(1311,6,'Bishop',0),
(1352,1,'Elder',0),
(1353,1,'Elder',0),
(1354,1,'Elder',0),
(1388,3,'ShillenElder',0),
(1389,3,'ShillenElder',0),
(1392,3,'Prophet',0),
(1393,3,'Elder',0),
(1397,3,'Elder',0),
(1355,1,'Elder',0),
(1356,1,'Prophet',0),
(1357,1,'ShillenElder',0),
(1363,1,'WarCryer',0),
(1414,1,'OverLord',0),
(4699,13,'Summon',0),
(4700,13,'Summon',0),
(4702,13,'Summon',0),
(4703,13,'Summon',0);






