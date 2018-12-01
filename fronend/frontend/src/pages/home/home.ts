// import { CalendarComponentOptions } from 'ion2-calendar';
// -> install 'npm i ion2-calendar'

import {Component, OnInit} from '@angular/core';
import {AlertController, Events, List, NavController, ToastController,App} from 'ionic-angular';
import { HttpClient, HttpHeaders } from '@angular/common/http';
//import { Certificate } from 'crypto';

import { ModalController } from 'ionic-angular';




@Component({
  selector: 'page-home',
  templateUrl: 'home.html',
})

/*
export interface Item{
  entry_time: String;
}
*/
export class HomePage implements OnInit{

   
  
    date1:string;
  

    constructor(public navCtrl: NavController,public http:HttpClient,public toastCtrl:ToastController, public modalCtrl: ModalController,private alertCtrl: AlertController,public app:App) {
    }
ionViewWillEnter(){

}
    


    presentToast(message: string) {
      let toast = this.toastCtrl.create({
        message: message,
        duration: 3000,
        position: 'bottom'
      });
      toast.present();
      return toast;
    }

    

  
getdata() {

    console.log("dddddd")
  
    let  myheaders = new HttpHeaders({});
    let formData=new FormData();

        this.http.get("http://localhost:3000/home", {headers: myheaders, withCredentials: true}).subscribe((data:any[])=>{


          var col = [];

          col.push("FDATE");
          col.push("CATEGORY");
          col.push("EXPENSES");
          col.push("COMMENT");



          //1. create dynamic table
          var table = document.createElement("table");
          table.setAttribute("width","100%");
          table.style.fontSize="1.75rem";


          //2. create html table header row using the extracted headers above
          //The value of -1 is, used, this results in a new row being inserted at the last position.
          var tr = table.insertRow(-1); //-> table row

        
          //3. add JSON data to the table as rows

          var rowNums = {};  
          rowNums['headers'] = 0;  // the zeroth row is the headers
          var recordIdArray = [];

          for(var k = 0; k < data.length; k++){
            tr = table.insertRow(-1); //second row 맨 아래부터 시작(그것을 뒤집음)
            var tabCell;
            var button1 = document.createElement('button');
            var button2 = document.createElement('button');
            button1.innerHTML = "VIEW";
            button2.innerHTML = "DELETE";
            button1.style.fontSize = "1.7rem";
            button2.style.fontSize = "1.7rem";
            button1.style.color="#98bce7";
            button2.style.color="red";
            let recordId = data[k]['RECORD_ID'];
            rowNums[recordId] = k + 1 //the k+1 row is the row for record id
            recordIdArray[k] = recordId;
            
            for(var h = 0; h <col.length; h++){
              tabCell = tr.insertCell(-1);  //insert new cells(<td> elements)
              tabCell.setAttribute("width","25%");
              tabCell.setAttribute("align","center");
              //맨 오른쪽 세로부터 시작(그것을 뒤집음)
              //var stuff = data[k][col[3]];
              if(h<3){
                tabCell.innerHTML = data[k][col[h]];

    
              }else{
                tabCell.appendChild(button1);
                tabCell.appendChild(button2)
               // tabCell.setAttribute("align","center");
                let comment = data[k][col[3]];
                
                //button1 -> view notes
                if(comment==""){    
                  button1.addEventListener("click", function() {
                    alert("No notes");
                  }); 
                }else{
                  button1.addEventListener("click", function() {
                    alert(comment);
                  }); 
                }
                
                //button2 -> delete
                if(button2){
                  button2.addEventListener("click", function() {
                 
                      var xhr = new XMLHttpRequest();
                      xhr.open("POST", 'http://localhost:3000/home', true);
                      xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
                      xhr.send(JSON.stringify({ id: recordId}));

                      let recordRowNum = rowNums[recordId];  //row number for the record
                      table.deleteRow(recordRowNum); //remove the row of the record on the screen
                      delete rowNums[recordId]
                      //removing record Id from recordIdArray
                      for (let i = 0; i < recordIdArray.length; i++) {
                        if (recordIdArray[i] == recordId) {
                          recordIdArray.splice(i,1);
                        }
                      }
                      //resetting row nums without deleted record
                      for (let i = 0; i <= recordIdArray.length; i++) {
                        rowNums[recordIdArray[i]] = i + 1;
                      }
                  }); 
                }
              }
            }
           
          }
          //4. finally add the newly created table with json data to a container
          //document.getElementById("showData") 메서드는 요소의 id 속성이 주어진 문자열과 일치하는 요소를 나타내는 element 객체를 반환한다. 
          var divContainer = document.getElementById("showData");
          divContainer.innerHTML = ""; //혹시 모를 쓰레기 값 제거
          divContainer.appendChild(table); //container에 위에서 만든 table 삽입
          divContainer.setAttribute("align","top");

        });
      }

      ngOnInit(){
        this.getdata();
      }


viewByDate(showData) {
  
      let  myheaders = new HttpHeaders({});
      let formData=new FormData();
      
        document.getElementById(showData).innerHTML = "";
      
        let date = this.date1; 
        
        

        this.http.get("http://localhost:3000/home", {headers: myheaders, withCredentials: true}).subscribe((data:any[])=>{
          var col = [];

          col.push("FDATE");
          col.push("CATEGORY");
          col.push("EXPENSES");
          col.push("COMMENT");



          //1. create dynamic table
          var table = document.createElement("table");
          table.style.fontSize="1.75rem";
          table.setAttribute("width","100%");


          
          //2. create html table header row using the extracted headers above
          //The value of -1 is, used, this results in a new row being inserted at the last position.
          var tr = table.insertRow(-1); //-> table row
          // table.setAttribute("width","auto");
          
          var rowNums = {};  
          rowNums['headers'] = 0;  // the zeroth row is the headers
          var recordIdArray = [];
          console.log(date);
          for(var k = 0; k < data.length; k++){ 
            if(data[k]['FDATE']==date){
              tr = table.insertRow(-1); //second row 맨 아래부터 시작(그것을 뒤집음)
              var tabCell;
              var button1 = document.createElement('button');
              var button2 = document.createElement('button');
              button1.innerHTML = "VIEW";
              button2.innerHTML = "DELETE";
              button1.style.fontSize = "1.7rem";
              //button2.style.fontSize = "1.7rem";
              button1.style.color="#98bce7";
              //button2.style.color="red";
              let recordId = data[k]['RECORD_ID'];
              rowNums[recordId] = k + 1 //the k+1 row is the row for record id
              recordIdArray[k] = recordId;

              for(var h = 0; h <col.length; h++){
              tabCell = tr.insertCell(-1);  //insert new cells(<td> elements)


              //맨 오른쪽 세로부터 시작(그것을 뒤집음)
              //var stuff = data[k][col[3]];
                tabCell.setAttribute("width","25%");
                tabCell.setAttribute("align","center");
              if(h<3){
                tabCell.innerHTML = data[k][col[h]];
                
              }else{
                tabCell.appendChild(button1);
                //tabCell.appendChild(button2)
               // tabCell.setAttribute("align","center");
                let comment = data[k][col[3]];
                
                //button1 -> view notes
                if(comment==""){    
                  button1.addEventListener("click", function() {
                    alert("No notes");
                  }); 
                }else{
                  button1.addEventListener("click", function() {
                    alert(comment);
                  }); 
                }
                
               
              }
              }
            }
            
            }
            var divContainer = document.getElementById("showData");

            divContainer.innerHTML = ""; //혹시 모를 쓰레기 값 제거
            
            divContainer.appendChild(table);
            divContainer.setAttribute("align","top");
      }); 
        
    
    }


    


}

