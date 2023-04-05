"use strict";

if (typeof Object.assign !== 'function') {
  // Must be writable: true, enumerable: false, configurable: true
  Object.defineProperty(Object, "assign", {
    value: function assign(target, varArgs) { // .length of function is 2
      'use strict';
      if (target === null || target === undefined) {
        throw new TypeError('Cannot convert undefined or null to object');
      }

      var to = Object(target);

      for (var index = 1; index < arguments.length; index++) {
        var nextSource = arguments[index];

        if (nextSource !== null && nextSource !== undefined) { 
          for (var nextKey in nextSource) {
            // Avoid bugs when hasOwnProperty is shadowed
            if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
              to[nextKey] = nextSource[nextKey];
            }
          }
        }
      }
      return to;
    },
    writable: true,
    configurable: true
  });
}

function _instanceof(left, right) { if (right != null && typeof Symbol !== "undefined" && right[Symbol.hasInstance]) { return !!right[Symbol.hasInstance](left); } else { return left instanceof right; } }

function _classCallCheck(instance, Constructor) { if (!_instanceof(instance, Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

var Sha256 = /*#__PURE__*/function () {
  function Sha256() {
    _classCallCheck(this, Sha256);
  }

  _createClass(Sha256, null, [{
    key: "hash",

    /**
     * Generates SHA-256 hash of string.
     *
     * @param   {string} msg - (Unicode) string to be hashed.
     * @param   {Object} [options]
     * @param   {string} [options.msgFormat=string] - Message format: 'string' for JavaScript string
     *   (gets converted to UTF-8 for hashing); 'hex-bytes' for string of hex bytes ('616263' = 'abc') .
     * @param   {string} [options.outFormat=hex] - Output format: 'hex' for string of contiguous
     *   hex bytes; 'hex-w' for grouping hex bytes into groups of (4 byte / 8 character) words.
     * @returns {string} Hash of msg as hex character string.
     */
    value: function hash(msg, options) {
      var defaults = {
        msgFormat: 'string',
        outFormat: 'hex'
      };
      var opt = Object.assign(defaults, options); // note use throughout this routine of 'n >>> 0' to coerce Number 'n' to unsigned 32-bit integer

      switch (opt.msgFormat) {
        default: // default is to convert string to UTF-8, as SHA only deals with byte-streams

        case 'string':
          msg = utf8Encode(msg);
          break;

        case 'hex-bytes':
          msg = hexBytesToString(msg);
          break;
        // mostly for running tests
      } // constants [§4.2.2]


      var K = [0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2]; // initial hash value [§5.3.3]

      var H = [0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19]; // PREPROCESSING [§6.2.1]

      msg += String.fromCharCode(0x80); // add trailing '1' bit (+ 0's padding) to string [§5.1.1]
      // convert string msg into 512-bit blocks (array of 16 32-bit integers) [§5.2.1]

      var l = msg.length / 4 + 2; // length (in 32-bit integers) of msg + ‘1’ + appended length

      var N = Math.ceil(l / 16); // number of 16-integer (512-bit) blocks required to hold 'l' ints

      var M = new Array(N); // message M is N×16 array of 32-bit integers

      for (var i = 0; i < N; i++) {
        M[i] = new Array(16);

        for (var j = 0; j < 16; j++) {
          // encode 4 chars per integer (64 per block), big-endian encoding
          M[i][j] = msg.charCodeAt(i * 64 + j * 4 + 0) << 24 | msg.charCodeAt(i * 64 + j * 4 + 1) << 16 | msg.charCodeAt(i * 64 + j * 4 + 2) << 8 | msg.charCodeAt(i * 64 + j * 4 + 3) << 0;
        } // note running off the end of msg is ok 'cos bitwise ops on NaN return 0

      } // add length (in bits) into final pair of 32-bit integers (big-endian) [§5.1.1]
      // note: most significant word would be (len-1)*8 >>> 32, but since JS converts
      // bitwise-op args to 32 bits, we need to simulate this by arithmetic operators


      var lenHi = (msg.length - 1) * 8 / Math.pow(2, 32);
      var lenLo = (msg.length - 1) * 8 >>> 0;
      M[N - 1][14] = Math.floor(lenHi);
      M[N - 1][15] = lenLo; // HASH COMPUTATION [§6.2.2]

      for (var _i = 0; _i < N; _i++) {
        var W = new Array(64); // 1 - prepare message schedule 'W'

        for (var t = 0; t < 16; t++) {
          W[t] = M[_i][t];
        }

        for (var _t = 16; _t < 64; _t++) {
          W[_t] = Sha256.s1(W[_t - 2]) + W[_t - 7] + Sha256.s0(W[_t - 15]) + W[_t - 16] >>> 0;
        } // 2 - initialise working variables a, b, c, d, e, f, g, h with previous hash value


        var a = H[0],
            b = H[1],
            c = H[2],
            d = H[3],
            e = H[4],
            f = H[5],
            g = H[6],
            h = H[7]; // 3 - main loop (note '>>> 0' for 'addition modulo 2^32')

        for (var _t2 = 0; _t2 < 64; _t2++) {
          var T1 = h + Sha256.S1(e) + Sha256.Ch(e, f, g) + K[_t2] + W[_t2];

          var T2 = Sha256.S0(a) + Sha256.Maj(a, b, c);
          h = g;
          g = f;
          f = e;
          e = d + T1 >>> 0;
          d = c;
          c = b;
          b = a;
          a = T1 + T2 >>> 0;
        } // 4 - compute the new intermediate hash value (note '>>> 0' for 'addition modulo 2^32')


        H[0] = H[0] + a >>> 0;
        H[1] = H[1] + b >>> 0;
        H[2] = H[2] + c >>> 0;
        H[3] = H[3] + d >>> 0;
        H[4] = H[4] + e >>> 0;
        H[5] = H[5] + f >>> 0;
        H[6] = H[6] + g >>> 0;
        H[7] = H[7] + h >>> 0;
      } // convert H0..H7 to hex strings (with leading zeros)


      for (var _h = 0; _h < H.length; _h++) {
        H[_h] = ('00000000' + H[_h].toString(16)).slice(-8);
      } // concatenate H0..H7, with separator if required


      var separator = opt.outFormat == 'hex-w' ? ' ' : '';
      return H.join(separator);
      /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

      function utf8Encode(str) {
        try {
          return new TextEncoder().encode(str, 'utf-8').reduce(function (prev, curr) {
            return prev + String.fromCharCode(curr);
          }, '');
        } catch (e) {
          // no TextEncoder available?
          return unescape(encodeURIComponent(str)); // monsur.hossa.in/2012/07/20/utf-8-in-javascript.html
        }
      }

      function hexBytesToString(hexStr) {
        var str = hexStr.replace(' ', ''); // allow space-separated groups

        return str == '' ? '' : str.match(/.{2}/g).map(function (byte) {
          return String.fromCharCode(parseInt(byte, 16));
        }).join('');
      }
    }
    /**
     * Rotates right (circular right shift) value x by n positions [§3.2.4].
     * @private
     */

  }, {
    key: "ROTR",
    value: function ROTR(n, x) {
      return x >>> n | x << 32 - n;
    }
    /**
     * Logical functions [§4.1.2].
     * @private
     */

  }, {
    key: "S0",
    value: function S0(x) {
      return Sha256.ROTR(2, x) ^ Sha256.ROTR(13, x) ^ Sha256.ROTR(22, x);
    }
  }, {
    key: "S1",
    value: function S1(x) {
      return Sha256.ROTR(6, x) ^ Sha256.ROTR(11, x) ^ Sha256.ROTR(25, x);
    }
  }, {
    key: "s0",
    value: function s0(x) {
      return Sha256.ROTR(7, x) ^ Sha256.ROTR(18, x) ^ x >>> 3;
    }
  }, {
    key: "s1",
    value: function s1(x) {
      return Sha256.ROTR(17, x) ^ Sha256.ROTR(19, x) ^ x >>> 10;
    }
  }, {
    key: "Ch",
    value: function Ch(x, y, z) {
      return x & y ^ ~x & z;
    } // 'choice'

  }, {
    key: "Maj",
    value: function Maj(x, y, z) {
      return x & y ^ x & z ^ y & z;
    } // 'majority'

  }]);

  return Sha256;
}();