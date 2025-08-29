Search
  = _ first:TermOrFilter rest:(_ TermOrFilter)* _ {
      return [first, ...rest.map(r => r[1])];
    }

TermOrFilter
  = InFilter
  / EqFilter
  / PlainTerm
  / NeqShortFilter


InFilter
  = value:Value In fields:Fields {
      return {
        type: "in",
        fields,
        value
      };
    }

In
  = [ \t\n\r]+ "in"i [ \t\n\r]+


NeqShortFilter
  = [!-] field:Field {
      return {
        type: "eq",
        negated: true,
        field,
        values: ["*"]
      };
    }


EqFilter
  = negated:Neg? field:Field ":" values:Values  {
      if (values[0] === '!') {
        values = ['*'];
        negated = '-';
      }
      return {
        type: "eq",
        negated: !!negated ,
        field,
        values: values
      };
    }

Neg
  = [-!] { return "-"; }

Field
  = head:[a-zA-Z0-9_] tail:[a-zA-Z0-9._-]* {
      return head + tail.join("");
    }

Fields
  = head:Field tail:("," Field)* {
      return [head, ...tail.map(t => t[1])];
    }

Values
  = "*" { return ["*"]; }
  / "!" { return ["!"]; }
  / head:Value tail:( "," Value)* {
      return [head, ...tail.map(t => t[1])];
    }

Value
  = QuotedValue
  / UnquotedValue

QuotedValue
  = "\"" chars:QuotedChars "\"" {
      return chars;
    }

QuotedChars
  = chars:([^"]*) { return chars.join(""); }

UnquotedValue
  = first:[^ \t\n\r,:\-!] rest:[^ \t\n\r,:]+ {
      return [first, ...rest].join("");
   }

PlainTerm
  = value:Value {
      return { type: "term", value };
    }

_ "whitespace" = [ \t\n\r]*
