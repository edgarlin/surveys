drop table surveys;
create table surveys (
SURVEYID bigint,
MODE     varchar(200),
FORMID   varchar(200),
DATAID   varchar(200),
DATAJSON json,
MEMO     varchar(200),
SAVETIME timestamp);

drop sequence SURVEYSEQ;
create sequence SURVEYSEQ;

insert into surveys (surveyid,formid,mode,memo,datajson) 
values (nextval('surveyseq'),'FORM1','FORM','MEMO1','{}');
insert into surveys (surveyid,formid,mode,memo,datajson) 
values (nextval('surveyseq'),'FORM2','FORM','MEMO2','{}');

delete from surveys where mode = 'DATA';

insert into surveys (surveyid,mode,formid,dataid,datajson,memo,savetime) 
values (nextval('surveyseq'),'FORM2','DATA','MEMO2','{}');

select current_timestamp;

select * from surveys where mode = 'DATA' and formid = 'FORM1';

update surveys set datajson = '{
 "pages": [
  {
   "name": "FormList",
   "elements": [
    {
     "type": "dropdown",
     "name": "FormList1",
     "title": "Form",
     "titleLocation": "left",
     "choices": [
      {
       "value": "V21",
       "text": "T21"
      },
      {
       "value": "V22",
       "text": "T22"
      },
      {
       "value": "V23",
       "text": "T23"
      }
     ]
    }
   ],
   "navigationButtonsVisibility": "hide"
  }
 ],
 "showPrevButton": false,
 "showTitle": false,
 "showPageTitles": false,
 "showQuestionNumbers": "off"
}' where formid = 'FORM2';




select * from surveys where surveyid =  (select max(surveyid) from surveys where mode = 'DATA' and formid = 'FORM1' group by formid)

