var express = require('express');
var router = express.Router();
var dbconnection = require('../dbConnection');
var bcrypt = require ('bcrypt')

var user_name;
async function get_user_profile(user_name,callback){
    var sql1 = "select profile.*, user.PASSWORD, Date_format(profile.BIRTH_DAY, '%y-%m-%d') as BD from profile,user where profile.USER_NAME= ?";

    let promises = [];
    promises[0] = new Promise(function (resolve,reject){
        dbconnection.query(sql1,[user_name],function(error,result){
            if (error){
                console.log("error message:",error);
            }
            jsond = JSON.stringify(result);
            resolve(jsond);
        });

    });
    var res = await promises[0];
    callback(res);
}

router.get('/', function (req, res) {
    if(req.session && req.session.user){
        this.user_name = req.session.user;
    }
    get_user_profile(this.user_name,function (result) {
        res.send(result);
    });

});

router.post('/',function(req,res){
    if (req.session && req.session.user) {
	var user= req.session.user;  //the saved session user_name from when user logged in
	var key = req.body.key;
	var value = req.body.value;
	var first_name = 'FIRST_NAME';
	var last_name = 'LAST_NAME';
	var age = 'AGE';
	var sex = 'SEX';
	var income = 'INCOME';
	var dob = 'BIRTH_DAY'
	var password = "PASSWORD"
	
	key_list = [first_name,last_name,age,sex,income,dob,password];
	if(key_list.contains(key)==false)
	{
            console.log('error');
            res.end("ERROR");
	}
	
	
	if (key == dob)
	{
            if (value === '' || value == null) {
		value = null
            } else {
		value = new Date(value);
            }
	}
	if (key == password)
	{
            update_password(req.body.old_password,req.body.new_password,user,function (result){
		res.end(result)
            });
	}
	else
	{
            update_user(key,value,user,function (result){
		res.end(result)
            });
	}
    } else {
	res.end("NOT_LOGGED_IN");
    }
})

function update_password (oldPassword,newPassword,user,callback)
{
    var sql = "SELECT * FROM user WHERE USER_NAME = ?";
    dbconnection.query(sql,[user], function (err, result) {
    if (err) {
        console.log(err);
        callback("ERR_DB_USER_GET");
    } else {
                var hash = bcrypt.hashSync(oldPassword,result[0].SALT);
                if (hash === result[0].PASSWORD) {
                    var salt = bcrypt.genSaltSync(10);
                    var hash = bcrypt.hashSync(newPassword,salt);
                    var sql = "UPDATE user SET PASSWORD = ?,SALT = ? WHERE USER_NAME = ?";
                    dbconnection.query(sql, [hash,salt,user], function (err, result) {
                        if (err) {
                        console.log(err);
                        callback("ERR_DB_PASSWORD_UPDATE");
                        } else {
                            callback("SUCCESS");
                        }
                    });
                } else {  
                    callback("INVALID_OLD_PASSWORD");
                }
            } 
        });   
}


function update_user (key,value,username,callback)
{
    dbconnection.query('SELECT * FROM profile WHERE USER_NAME = ?', [username],function (error, results, fields) {
        if (error) {
            console.log("error ocurred", error);
        } else {
            if (results.length > 0) {
                sql = 'UPDATE profile SET ' +key +' = ? WHERE USER_NAME = ?';
                dbconnection.query(sql, [value,username], function (error, results, fields) {
                    if (error) {
                        console.log("error", error);
                    }
                    else
                    {
                        callback("SAVED");
                    }
                });
            }
        }
    });
}

Array.prototype.contains = function ( needle ) {
    for (i in this) {
        if (this[i] == needle) return true;
    }
    return false;
}

module.exports = router;
