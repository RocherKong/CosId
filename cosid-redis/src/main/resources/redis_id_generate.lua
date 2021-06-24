local namespace = KEYS[1];
local name = KEYS[2];
local step = tonumber(ARGV[1]);

local adderKey = 'cosid' .. ':' .. namespace .. ':' .. name .. ':adder';
redis.call('setnx', adderKey, 0);
return redis.call("incrby", adderKey, step);
