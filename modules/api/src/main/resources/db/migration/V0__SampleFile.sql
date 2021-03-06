--Flyway is an open-source database migration tool. It strongly favors simplicity and convention over configuration.
--
--It is based around just 6 basic commands: Migrate, Clean, Info, Validate, Baseline and Repair.
--
--Migrations can be written in SQL (database-specific syntax (such as PL/SQL, T-SQL, ...) is supported) or Java (for advanced data transformations or dealing with LOBs).

--Versions
--Each migration must have a unique version and a description.
--
--A version must have the following structure:
--
--One or more numeric parts
--Separated by a dot (.) or an underscore (_)
--Underscores are replaced by dots at runtime
--Leading zeroes are ignored in each part
--Examples of valid versions:
--
--1
--001
--5.2
--5_2 (5.2 at runtime)
--1.2.3.4.5.6.7.8.9
--205.68
--20130115113556
--2013.1.15.11.35.56
--2013.01.15.11.35.56

--A example of a migration field could be:

--create table PERSON (
--    ID int not null,
--    NAME varchar(100) not null
--);