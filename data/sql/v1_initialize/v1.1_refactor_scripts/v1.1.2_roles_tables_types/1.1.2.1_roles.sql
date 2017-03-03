# CREATE USER 'no_admin'@'%'
#   IDENTIFIED BY 'no_admin';
# CREATE USER 'no_app'@'%'
#   IDENTIFIED BY 'no_app';
#
# GRANT USAGE ON no_waiting.* TO 'no_app'@'%' IDENTIFIED by 'no_app';
# GRANT USAGE ON no_waiting.* TO 'no_app'@'localhost' IDENTIFIED by 'no_app';
# GRANT INSERT, DELETE ON no_waiting.* TO 'no_app'@'%' IDENTIFIED by 'no_app';
# GRANT INSERT, DELETE ON no_waiting.* TO 'no_app'@'localhost' IDENTIFIED by 'no_app';
# GRANT SELECT, UPDATE ON no_waiting.* TO 'no_app'@'%' IDENTIFIED by 'no_app';
# GRANT SELECT, UPDATE ON no_waiting.* TO 'no_app'@'localhost' IDENTIFIED by 'no_app';
#
# GRANT USAGE ON no_waiting.* TO 'no_admin'@'%' IDENTIFIED by 'no_admin';
# GRANT USAGE ON no_waiting.* TO 'no_admin'@'localhost' IDENTIFIED by 'no_admin';
# GRANT ALL PRIVILEGES ON no_waiting.* TO 'no_admin'@'%'IDENTIFIED by 'no_admin';
# GRANT ALL PRIVILEGES ON no_waiting.* TO 'no_admin'@'localhost' IDENTIFIED by 'no_admin';
