var surveyForm_List = {
  pages: [
    {
      name: "FormPage",
      elements: [
        {
          type: "dropdown",
          name: "FORMID",
          title: "Form",
          titleLocation: "left",
          choicesByUrl: {
            url: "./proc",
            valueName: "FORMID",
            titleName: "MEMO"
          }
        }
      ],
      navigationButtonsVisibility: "hide"
    }
  ],
  showPrevButton: false,
  showTitle: false,
  showPageTitles: false,
  showQuestionNumbers: "off"
};

window.surveyObj_List = new Survey.Model(surveyForm_List);

surveyObj_List.onComplete.add(function(result) {});

surveyObj_List.onValueChanged.add(function(sender, options) {
  var mySurvey = sender;
  var questionName = options.name;
  var newValue = options.value;
  $(".form-loaded").hide();
});

$("#surveyList").Survey({
  model: surveyObj_List
});

var funcPROC = function(_action, _parm) {
  var parmJSON = JSON.stringify(_parm);
  var respJSON;
  //$("#surveyResult").html(parmJSON);
  $.ajax({
    async: false,
    type: "post",
    url: "./proc",
    dataType: "JSON",
    data: {
      action: _action,
      json: parmJSON
    },
    success: function(resp) {
      console.log("success");
      respJSON = resp;
    },
    error: function(resp) {
      console.log("error");
      respJSON = resp;
    }
  });
  return respJSON;
};

// load latest FORM DATA
var loadLastestData = function() {
  var resp = funcPROC("DATA", $("#hiddenFORM").serializeArray());
  $("#hidSURVEYID").val(resp.SURVEYID || "");
  //$("#hidDATAJSON").val(resp.DATAJSON || "{}");
  surveyObj_Form.data = JSON.parse(resp.DATAJSON || {});
};

// load FORM
$("#btnLOAD").click(function() {
  var resp = funcPROC("FORM", surveyObj_List.data);
  window.surveyObj_Form = new Survey.Model(resp.DATAJSON);
  $("#surveyForm").Survey({
    model: surveyObj_Form
  });
  $("#hidFORMID").val(resp.FORMID);
  loadLastestData();
  $(".form-loaded").show();
});

// insert FORM DATA
$("#btnSAVE").click(function() {
  $("#hidDATAJSON").val(JSON.stringify(surveyObj_Form.data));
  var resp = funcPROC("SAVE", $("#hiddenFORM").serializeArray());
  loadLastestData();
});

// read previous FORM DATA
$("#btnPREV").click(function() {
  var resp = funcPROC("PREV", $("#hiddenFORM").serializeArray());
  $("#hidSURVEYID").val(resp.SURVEYID);
  surveyObj_Form.data = JSON.parse(resp.DATAJSON || {});
});

// read next FORM DATA
$("#btnNEXT").click(function() {
  var resp = funcPROC("NEXT", $("#hiddenFORM").serializeArray());
  $("#hidSURVEYID").val(resp.SURVEYID);
  surveyObj_Form.data = JSON.parse(resp.DATAJSON || {});
});

$(".form-loaded").hide();
