var dbconnection = require('./dbConnection');

var income_categories = ["salary","part-time","financial","other"];
var cost_categories = ["food","drink","traffic","shopping","entertainment","home","electronics","medical"];
var icon_name = ["NA","NA","card","logo-usd","logo-bitcoin","globe","pizza","beer","bus","pricetag","game-controller-a","home","phone-portrait","medical"];

var table_name1 = "profile";
var sql1 = "CREATE TABLE profile (USER_NAME VARCHAR(20) NOT NULL UNIQUE, FIRST_NAME VARCHAR(20) NOT NULL, LAST_NAME VARCHAR(20),AGE INT, BIRTH_DAY DATE, SEX VARCHAR(20), INCOME FLOAT)";
var table_name2 = "user";
var sql2 = "CREATE TABLE user (USER_NAME VARCHAR(20) NOT NULL UNIQUE, PASSWORD CHAR(60), SALT CHAR(29))";
var table_name3 = "expenses";
var sql3 = "CREATE TABLE expenses (USER_NAME VARCHAR(20) NOT NULL, EXPENSES FLOAT, CATEGORY VARCHAR(20), DATE DATETIME)";
var table_name4 = "category";
var sql4 = "CREATE TABLE category (CATEGORY_ID INT NOT NULL UNIQUE PRIMARY KEY AUTO_INCREMENT, CATEGORY VARCHAR(20), PARENT_ID INT NOT NULL, ICON VARCHAR(20))";
var table_name5 = "record";
var sql5 = "CREATE TABLE record (RECORD_ID INT NOT NULL UNIQUE PRIMARY KEY AUTO_INCREMENT, USER_NAME VARCHAR(20) NOT NULL, CATEGORY_ID INT NOT NULL, CATEGORY VARCHAR(20) NOT NULL, PARENT_ID INT NOT NULL, COMMENT VARCHAR(200), MONEY FLOAT, DATE DATETIME NOT NULL)";
var table_name6 = "UserCategory";
var sql6 = "CREATE TABLE UserCategory (USER_NAME VARCHAR(20) NOT NULL UNIQUE, U_CATEGORYS VARCHAR(100) NOT NULL)";
//USER_NAME,CATEGORY_ID,CATEGORY,PARENT_ID,COMMENT,MONEY,DATE

function replace_table(table_name,sql) {
    return new Promise(function(resolve,reject) {
	//checking if table exists
	var sql_test = "SELECT count(*) AS table_exists FROM information_schema.TABLES WHERE (TABLE_SCHEMA = 'app') AND (TABLE_NAME = '"+table_name+"')";  //checks if table exists
	dbconnection.query(sql_test, function (err, result) {
	    if (err) throw err;
	    table_exists = result[0].table_exists;
	    if (table_exists) {
		console.log(table_name + " exists")
		//dropping table if it already exists
		var sql_drop = "DROP TABLE " + table_name;
		dbconnection.query(sql_drop, function(err,result) {
		    if (err) throw err;
		    console.log("dropped " + table_name);
		    //adding table back to database
		    dbconnection.query(sql,function(err,result) {
			if (err) throw err;
			console.log("added " + table_name);
			resolve();
		    });
		});
	    } else {
		//adding table to database
		dbconnection.query(sql,function(err,result) {
		    if (err) throw err;
		    console.log("added " + table_name);
		    resolve();
		});
	    }
	});
    });
}

function get_category_id(category, callback) {
    var sql_test = "SELECT CATEGORY_ID FROM category WHERE CATEGORY = ?";
    dbconnection.query(sql_test, [category], function (err, result) {
	if (err) throw err;
	callback(result[0].CATEGORY_ID);
    });
};

function load_category(category,parent_uid) {
    sql = "INSERT INTO category (CATEGORY, PARENT_ID) VALUES (?,?)";
    dbconnection.query(sql, [category,parent_uid], function (err, result) {
	if (err) throw err;
	console.log("inserted " + category + " into category table with parentId=" +  parent_uid);
    });
}
console.log(icon_name[5]);


function load_all_categories() {
    return new Promise(function(resolve,reject) {
	load_category("income",0);
	load_category("cost",0);
	//adding income categories
	get_category_id("income", function(parent_uid) {
	    income_categories.forEach( element => {
		load_category(element,parent_uid);
	    });
	    //adding cost
		get_category_id("cost", function(parent_uid) {
		cost_categories.forEach( element => {
		    load_category(element,parent_uid);
		});
		for (i=0;i<icon_name.length;i++){
			sql = "UPDATE category SET ICON=? WHERE CATEGORY_ID=?+1";
			dbconnection.query(sql,[icon_name[i],i],function (err, result) {
				if (err) throw err;
			});
		}
		resolve();
	    });
	});
    });

}

async function replace_all_tables() {
    let promises = [];
    promises[0] = replace_table(table_name1,sql1);
    promises[1] = replace_table(table_name2,sql2);
    promises[2] = replace_table(table_name3,sql3);
    promises[3] = replace_table(table_name4,sql4);
    promises[4] = replace_table(table_name5,sql5);
    promises[5] = replace_table(table_name6,sql6);
    await Promise.all(promises);
    await load_all_categories();
    dbconnection.end();
}

replace_all_tables();

