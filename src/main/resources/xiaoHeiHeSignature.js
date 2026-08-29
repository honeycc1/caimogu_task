function d3(e) {
    return e & 128 ? (e << 1 ^ 27) & 255 : e << 1
}
function Mc(e) {
    return d3(e) ^ e
}
function _f(e) {
    return Mc(d3(e))
}
function Lh(e) {
    return _f(Mc(d3(e)))
}
function sg(e) {
    return Lh(e) ^ _f(e) ^ Mc(e)
}
function kwe(e) {
    var t = [0, 0, 0, 0];
    t[0] = sg(e[0]) ^ Lh(e[1]) ^ _f(e[2]) ^ Mc(e[3]);
    t[1] = Mc(e[0]) ^ sg(e[1]) ^ Lh(e[2]) ^ _f(e[3]);
    t[2] = _f(e[0]) ^ Mc(e[1]) ^ sg(e[2]) ^ Lh(e[3]);
    t[3] = Lh(e[0]) ^ _f(e[1]) ^ Mc(e[2]) ^ sg(e[3]);
    e[0] = t[0];
    e[1] = t[1];
    e[2] = t[2];
    e[3] = t[3];
    return e;
}
function IM(e, t, n) {
    var r = "", o = t.slice(0, n);
    for (var a = 0; a < e.length; a++) {
        var i = e.charCodeAt(a) % o.length;
        r += o[i];
    }
    return r;
}
function OM(e, t) {
    var n = "";
    for (var r = 0; r < e.length; r++) {
        var a = e.charCodeAt(r) % t.length;
        n += t[a];
    }
    return n;
}
function Twe(e) {
    var t = "";
    // 1. 获取每个子数组长度
    var lenArr = [];
    for(var k=0;k<e.length;k++){
        lenArr.push(e[k].length);
    }
    // 2. 手动求最大长度，替代 Math.max(...e.map(r=>r.length))
    var maxLen = 0;
    for(var m=0;m<lenArr.length;m++){
        if(lenArr[m] > maxLen){
            maxLen = lenArr[m];
        }
    }
    // 3. 模拟原来 forEach + 箭头逻辑
    for (var n = 0; n < maxLen; n++) {
        for(var j=0;j<e.length;j++){
            var r = e[j];
            if(n < r.length){
                t += r[n];
            }
        }
    }
    return t;
}
function Awe(e) {
    // 替换 reduce((t,n)=>t+n,0)
    var sum = 0;
    for(var i=0;i<e.length;i++){
        sum = sum + e[i];
    }
    return sum;
}
function Tr_1(e, t, n) {
    // 替换 e.split("/").filter(f=>f)
    var splitArr = e.split("/");
    var filterArr = [];
    for(var k=0;k<splitArr.length;k++){
        if(splitArr[k]){
            filterArr.push(splitArr[k]);
        }
    }
    e = "/".concat(filterArr.join("/"), "/");
    var r = "AB45STUVWZEFGJ6CH01D237IXYPQRKLMN89",
        o = IM(String(t), r, -2),
        a = OM(e, r),
        s = OM(n, r),
        i = Twe([o, a, s]).slice(0, 20);
    return i;
}


function Tr_2(l) {
    var r = "AB45STUVWZEFGJ6CH01D237IXYPQRKLMN89"
    var sliceStr = l.slice(-6);
    var charArr = [];
    for(var i=0;i<sliceStr.length;i++){
        charArr.push(sliceStr.charCodeAt(i));
    }
    var kweRes = kwe(charArr);
    var aweVal = Awe(kweRes);
    var u = "" + (aweVal % 100);
    if(u.length < 2){
        u = "0" + u;
    }
    var c = IM(l.substring(0, 5), r, -4);
    return "" + c + u;
}
