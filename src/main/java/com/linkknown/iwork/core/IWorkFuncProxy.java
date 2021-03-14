package com.linkknown.iwork.core;

import com.linkknown.iwork.core.exception.IWorkException;
import com.linkknown.iwork.util.DatatypeUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class IWorkFuncProxy {


//    // 将 sql 查询的多条记录根据字段名转换成切片
//    func (t *IWorkFuncProxy) TransformSqlQueryRowsToSliceByKey(args []interface{}) interface{} {
//        result := make([]interface{}, 0)
//        key := args[1].(string)
//                rowDatas := args[0].([]map[string]interface{}) // 查询出来的数据
//        for _, rowData := range rowDatas {
//            result = append(result, rowData[key])
//        }
//        return result
//    }

    public Object getFileNameFromUrl(Object... args) throws IWorkException {
        String[] arr = StringUtils.splitByWholeSeparator(StringUtils.trim(((String) args[0])), "/");
        String fileName = arr[arr.length - 1];
        if (StringUtils.isNotEmpty(fileName)) {
            return fileName;
        }
        throw new IWorkException(String.format("[url=%s] 未找到有效的文件名", ((String) args[0])));
    }

    public Object True(Object... args) {
        return true;
    }

    public Object False(Object... args) {
        return false;
    }

    public Object randNumberString(Object... args) {
        int width = DatatypeUtil.objectToInt(args[0]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            sb.append(new Random().nextInt(10));
        }
        return sb.toString();
    }

    public Object generateMap(Object... args) {
        Map<String, Object> map = new HashMap<>();
        if (args != null && args.length % 2 == 0) {
            for (int index = 0; index < args.length; index++) {
                if (index % 2 == 0) {
                    map.put((String) args[index], args[index + 1]);
                }
            }
        } else {
            new IWorkException("generateMap func args length is mismatch!");
        }
        return map;
    }

//    func (t *IWorkFuncProxy) AesEncrypt(args []interface{}) interface{} {
//        return chiperutil.AesEncryptToStr(args[0].(string), args[1].(string))
//    }
//
//    func (t *IWorkFuncProxy) AesDecrypt(args []interface{}) interface{} {
//        return chiperutil.AesDecryptToStr(args[0].(string), args[1].(string))
//    }
//
//    func (t *IWorkFuncProxy) BcryptGenerateFromPassword(args []interface{}) interface{} {
//        hashedPassword, err := chiperutil.BcryptGenerateFromPassword(args[0].(string))
//        errorutil.CheckError(err)
//        return hashedPassword
//    }
//
//    func (t *IWorkFuncProxy) BcryptCompareHashAndPassword(args []interface{}) interface{} {
//        err := chiperutil.BcryptCompareHashAndPassword(args[0].(string), args[1].(string))
//        return err == nil
//    }

    public Object sliceLen(Object... args) {
        if (args[0].getClass().isArray()) {
            return ((Object[]) args[0]).length;
        }
        return ((List<?>) args[0]).size();
    }

    public Object formatNowTimeToYYYYMMDD(Object... args) {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public Object isEmail(Object... args) {
        return ((String) args[0]).matches("^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$");
    }

    public Object isPhone(Object... args) {
        return ((String) args[0]).matches("^[1][3,4,5,7,8][0-9]{9}$");
    }

    public Object parseTimestampStrToDate(Object... args) {
        String timestamp = ((String) args[0]);
        Date date = new Date();
        date.setTime(Long.parseLong(timestamp));
        return timestamp;
    }

    public Object fmtSprintf(Object... args) {
        return String.format((String) args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    public Object getNotEmpty(Object... args) {
        for (Object _arg : args) {
            if (_arg != null) {
                return _arg;
            }
        }
        return null;
    }

    public Object getDomain(Object... args) {
        String url = ((String) args[0]);
        if (StringUtils.contains(url, "//")) {
            url = StringUtils.splitByWholeSeparator(url, "//")[1];
            return StringUtils.splitByWholeSeparator(url, "/")[0];
        }
        return "";
    }

    // TODO 返回数组参数
    public Object getRequestParameters(Object... args) {
        String urlAddress = ((String) args[0]);
        String paramName = ((String) args[1]);

        String params = urlAddress.substring(urlAddress.indexOf("?") + 1, urlAddress.length());
        String[] paramStrArr = StringUtils.splitByWholeSeparator(params, "&");
        for (String paramStr : paramStrArr) {
            String[] paramArr = StringUtils.splitByWholeSeparator(paramStr, "=");
            if (StringUtils.equals(paramName, paramArr[0])) {
                return paramArr[1];
            }
        }
        return null;
    }

//    func (t *IWorkFuncProxy) GetRequestParameters(args []interface{}) interface{} {
//        urlAddress := args[0].(string)
//                paramName := args[1].(string)
//                u, err := url.Parse(urlAddress)
//        if err != nil {
//            panic(err)
//        }
//        values, err := url.ParseQuery(u.RawQuery)
//        if err != nil {
//            panic(err)
//        }
//        return values[paramName]
//    }

    public Object getRequestParameter(Object... args) {
        return ((String[]) getRequestParameters(args))[0];
    }

    public Object stringsOneOf(Object... args) throws IWorkException {
        if (args.length > 1) {
            return Arrays.asList(args).stream().filter(obj -> StringUtils.equals(((String) args[0]), ((String) obj))).count() > 1;
        }
        throw new IWorkException("stringsOneOf func args length is mismatch!");
    }

    public Object stringsTrimPrefix(Object... args) {
        return StringUtils.removeStart(((String) args[0]), ((String) args[1]));
    }

    public Object stringsTrimSuffix(Object... args) {
        return StringUtils.removeEnd(((String) args[0]), ((String) args[1]));
    }

    public Object stringsRepeatQuestion(Object... args) {
        String str = StringUtils.repeat("?,", DatatypeUtil.objectToInt(args[0]));
        return StringUtils.removeEnd(str, ",");
    }

    public Object stringsRepeatWithSep(Object... args) {
        String str = StringUtils.repeat(((String) args[0]) + ((String) args[1]), DatatypeUtil.objectToInt(args[2]));
        return StringUtils.removeEnd(str, (String) args[1]);
    }

    public Object stringsRepeat(Object... args) {
        return StringUtils.repeat((String) args[0], DatatypeUtil.objectToInt(args[1]));
    }

    public Object stringsSplit(Object... args) {
        return StringUtils.splitByWholeSeparator((String) args[0], (String) args[1]);
    }

    public Object stringsEq(Object... args) {
        return StringUtils.equals(((String) args[0]), ((String) args[1]));
    }

    public Object stringsNotEq(Object... args) {
        return !StringUtils.equals(((String) args[0]), ((String) args[1]));
    }

    public Object stringsContains(Object... args) {
        return StringUtils.contains(((String) args[0]), ((String) args[1]));
    }

    public Object stringsHasSuffix(Object... args) {
        return StringUtils.endsWith(((String) args[0]), ((String) args[1]));
    }

    public Object stringsHasPrefix(Object... args) {
        return StringUtils.startsWith(((String) args[0]), ((String) args[1]));
    }

    public Object stringsToLower(Object... args) {
        return StringUtils.lowerCase(((String) args[0]));
    }

    public Object stringsToUpper(Object... args) {
        return StringUtils.upperCase(((String) args[0]));
    }

//
//    func (t *IWorkFuncProxy) StringsJoin(args []interface{}) interface{} {
//        sargs := make([]string, 0)
//        for _, arg := range args {
//            if arr, err := strconvToSlice(arg); err == nil {
//                sargs = append(sargs, arr...)
//            } else {
//                panic(err)
//            }
//        }
//        return strings.Join(sargs, "")
//    }
//
//    func strconvToSlice(s interface{}) ([]string, error) {
//        result := make([]string, 0)
//        if arr, ok := s.([]string); ok {
//            for _, val := range arr {
//                result = append(result, val)
//            }
//        } else if val, ok := s.(string); ok {
//            result = append(result, val)
//        } else {
//            return nil, errors.New(fmt.Sprintf(`convert error, %s is not string or []string`, s))
//        }
//        return result, nil
//    }

    public Object int64Add(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Add func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) + DatatypeUtil.objectToInt(args[1]);
    }

    public Object int64Sub(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Sub func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) - DatatypeUtil.objectToInt(args[1]);
    }

    public Object int64Gt(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Gt func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) > DatatypeUtil.objectToInt(args[1]);
    }

    public Object int64Lt(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Lt func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) < DatatypeUtil.objectToInt(args[1]);
    }

    public Object int64Eq(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Eq func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) == DatatypeUtil.objectToInt(args[1]);
    }

    public Object int64NotEq(Object... args) throws IWorkException {
        return !(boolean) int64Eq(args);
    }

    public Object int64Multi(Object... args) throws IWorkException {
        if (args.length != 2) {
            throw new IWorkException("int64Multi func args length is mismatch!");
        }
        return DatatypeUtil.objectToInt(args[0]) * DatatypeUtil.objectToInt(args[1]);
    }

    public Object stringsJoinWithSep(Object... args) {
        List<String> lst = Arrays.stream(args).map(o -> (String) o).collect(Collectors.toList());
        return StringUtils.join(lst.subList(0, lst.size() - 1), lst.get(lst.size() - 1));
    }

    // 判断多个值是否有一个为 true
    public Object or(Object... args) {
        return Arrays.stream(args).anyMatch(o -> o instanceof Boolean && (boolean) o);
    }

    // 判断多个值是否都为 true
    public Object and(Object... args) {
        return Arrays.stream(args).filter(o -> o instanceof Boolean && (boolean) o).count() == args.length;
    }

    public Object not(Object... args) {
        List<Boolean> lst = Arrays.stream(args).map(o -> o instanceof Boolean && (boolean) o).collect(Collectors.toList());
        return !lst.get(0);
    }

    public Object uuid(Object... args) {
        return UUID.randomUUID().toString();
    }

    public Object isEmpty(Object... args) {
        return args == null || args[0] == null;
    }

    public Object isNotEmpty(Object... args) {
        return !(boolean) this.isEmpty(args);
    }

    public Object pathJoin(Object... args) {
        List<String> lst = Arrays.stream(args).map(o -> (String) o).collect(Collectors.toList());
        return StringUtils.join(lst, "/");
    }

    public Object getDirPath(Object... args) {
        return new File(((String) args[0])).getParentFile().getAbsolutePath();
    }

    public Object ifThenElse(Object... args) throws IWorkException {
        if (args == null || args.length < 3) {
            throw new IWorkException("ifThenElse func args length is mismatch!");
        }
        // 参数为空条件为假
        if (args[0] == null) {
            return args[2];
        }
        if (args[0] instanceof Boolean) {
            // bool 值且 true
            // bool 值且 false
            return (boolean) args[0] ? args[1] : args[2];
        } else {
            // 非空且不是 bool 值为真
            return args[1];
        }
    }

    public Object switchCase(Object... args) throws IWorkException {
        if (args == null || args.length % 2 != 0) {
            throw new IWorkException("switchCase func args length is mismatch!");
        }
        for (int index = 0; index < args.length; index++) {
            if (index % 2 == 0) {
                if (args[index] instanceof Boolean && (Boolean) args[index]) {
                    return args[index + 1];
                }
            }
        }
        throw new IWorkException("所有条件都不满足");
    }

    public Object int64(Object... args) throws IWorkException {
        if (args[0] instanceof Integer) {
            return args[0];
        } else if (args[0] instanceof String) {
            return Integer.parseInt(((String) args[0]));
        }
        throw new IWorkException(args[0] + "转换成 int64 失败");
    }

    public Object getByteSizeForMB(Object... args) {
        return 1024 * 1024 * DatatypeUtil.objectToInt(args[0]);
    }

    public Object batchSqlBinding(Object... args) {
        List<List<Object>> lst = Arrays.stream(args).map(DatatypeUtil::objectConvertToSlice).collect(Collectors.toList());
        int maxLen = 0;
        for (List<Object> _lst : lst) {
            maxLen = Math.max(maxLen, _lst.size());
        }

        final int _maxLen = maxLen;
        lst.stream().forEach(_lst -> {
            for (int i = _lst.size(); i < _maxLen; i++) {
                _lst.add(_lst.get(0));
            }
        });

        List<Object> _lst = new ArrayList<>();
        lst.forEach(_lst::addAll);
        return _lst;
    }

}
