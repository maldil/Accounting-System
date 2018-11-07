var express = require('express');
var dbconnection = require('../dbConnection');
var router = express.Router();

async function get_recent_transactions(user_name,elements,callback) {
    console.log('request received');
    console.log(user_name);
    console.log(elements);
    var sql1 = "select * from expenses where USER_NAME= 'nablec'";
    let promises = [];
    promises[0] = new Promise(function (resolve,reject) {
        dbconnection.query(sql1, [user_name], function (error, results) {
            if (error) {
              console.log("error ocurred", error);
            }
            jsond = JSON.stringify(results);
                        //   res.render('Home', {title:'Home', home:jsond});
            resolve(jsond);
            console.log(jsond);
          });

    });
    var res = await promises[0];
    callback(res)
}



/* GET recent transactions listing. */
router.get('/', function(req, res) {
    var user_name = req.body.user;
    var elements = req.body.elements;
    get_recent_transactions(user_name,elements,function(result){
        res.send(result);
    });

});

module.exports = router;
