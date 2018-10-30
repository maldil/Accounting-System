var express = require('express');
var router = express.Router();
var dbconnection = require('../dbConnection');

// router.get('/',function (req,res) {
//     var user_name = req.body.user;
//     dbconnection.query("select USER_NAME,FIRST_NAME,LAST_NAME,BIRTH_DAY, AGE, SEX, INCOME from profile", function (err, rows) {
//         if(err){
//             res.render("profile",{title:" ",datas:[]});
//         }else {
//             res.render("profile",{title:" ",datas:rows});
//         }
//     });
// // });
// router.get('/', function (req, res, next) {
//     res.render('viewProfile', {title:'viewProfile'});
// });
// router.post('/viewProfile', function (req, res, next) {
//     var user_name = "BobSmith";
//     var sql1 = "select USER_NAME,FIRST_NAME,LAST_NAME,BIRTH_DAY, AGE, SEX, INCOME from profile where USER_NAME= ?";
//     var sql2 = "select USER_NAME, PASSWORD from users where USER_NAME= ?";
//     var str= "xxx ";
//     dbconnection.query(sql1, [user_name], function (err, result) {
//         if(err){
//             throw err;
//         }else {
//             var first_name = result[0].FIRST_NAME;
//             var last_name = result[0].LAST_NAME;
//             var birth_day = result[0].BIRTH_DAY;
//             var sex = result[0].SEX;
//             var income = result[0].INCOME
//             str = JSON.stringify(result);
//             res.send('viewProfile', {firstname: first_name});
//         }
//     });
// })

var user_name = "BobSmith";
var sql1 = "select USER_NAME,FIRST_NAME,LAST_NAME,BIRTH_DAY, AGE, SEX, INCOME from profile where USER_NAME= ?";
var sql2 = "select USER_NAME, PASSWORD from users where USER_NAME= ?";
var str= "xxx ";

router.get('/', function (req, res) {
    dbconnection.query(sql1, [user_name], function (err, result) {
        if(err) {
            console.log("error:", err.message);
        }
        var first_name = result[0].FIRST_NAME;
        var last_name = result[0].LAST_NAME;
        var birth_day = result[0].BIRTH_DAY;
        var sex = result[0].SEX;
        var income = result[0].INCOME
        str = JSON.stringify(result);
        res.render('viewProfile', {title:'viewProfile', profile:str});
        res.send(str);
    });
});
// router.get('/', function (req, res) {
//     res.render('viewProfile');
// });
// router.post('/javascript/viewProfilefroentend', {firstname: first_name, lastname: last_name, birthday: birth_day, sex: sex, income: income});
// router.get('/viewProfile', function (req, res) {
//     res.send(str);
// });
router.post('/',function(req,res){
    var first_name = req.body.first_name;
    var last_name = req.body.last_name;
    var age = req.body.age;
    var sex = req.body.sex;
    var income = req.body.income;
    var dob;
    if (req.body.dob === '' || req.body.dob == null) {
	dob = null
    } else {
	dob = new Date(req.body.dob);
    }
    var user_name = req.body.user;
    var password = req.body.password;
    if (user_name === "" || user_name == null) {
	res.end("ERR_NO_USER");
    } else if (password === "" || password == null) {
	res.end("ERR_NO_PASS");
    } else if (first_name === "" || first_name == null) {
	res.end("ERR_NO_FNAME");
    } else {
	
        update_user(first_name,last_name,dob,age,sex,income,user_name, function (result) {
	    res.end(result);
	});
    }

})

function update_user (first_name,last_name,dob,age,sex,income,username,callback)
{
    dbconnection.query('SELECT * FROM profile WHERE USER_NAME = ?', [username], function (error, results, fields) {
        if (error) {
          console.log("error ocurred", error);
        } else {
          if (results.length > 0) {
            dbconnection.query('UPDATE profile SET FIRST_NAME = ?,LAST_NAME = ?,BIRTH_DAY = ?,AGE = ?, SEX = ?, INCOME = ? WHERE USER_NAME = ?', [first_name,last_name,dob,age,sex,income, username], function (error, results, fields) {
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
module.exports = router;