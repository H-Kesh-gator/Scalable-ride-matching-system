'use strict';
const $ = id => document.getElementById(id);
let places = [], quoteVersion = 0;
function message(text, error=false) { $('message').textContent=text; $('message').className=error?'error':''; }
async function api(path, body) {
  const response = await fetch('/api/demo'+path, body ? {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)} : {});
  if(!response.ok) { let data={}; try{data=await response.json();}catch{} throw new Error(data.message || `Request failed (${response.status}). Please try again.`); }
  return response.json();
}
function request() {return {rider:$('rider').value.trim() || 'Guest',pickup:Number($('pickup').value),dropoff:Number($('dropoff').value),category:document.querySelector('[name=category]:checked').value};}
const money = value => new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(value);
function svg(tag, attrs) {const node=document.createElementNS('http://www.w3.org/2000/svg',tag);Object.entries(attrs).forEach(([k,v])=>node.setAttribute(k,v));return node;}
function point(p) {return {x:70+(p.lon-77.59)/.17*440,y:330-(p.lat-12.91)/.08*250};}
function draw() {
  const group=$('places-layer');group.replaceChildren();const r=request();
  places.forEach((p,i)=>{const pos=point(p);group.append(svg('circle',{cx:pos.x,cy:pos.y,r:i===r.pickup||i===r.dropoff?9:5,fill:i===r.pickup?'#8aa832':'#263d34',stroke:'#fff','stroke-width':3}));const label=svg('text',{x:pos.x+12,y:pos.y-12});label.textContent=p.name;group.append(label);});
  const a=point(places[r.pickup]),b=point(places[r.dropoff]);$('route-layer').replaceChildren(svg('path',{d:`M${a.x} ${a.y} L${b.x} ${b.y}`,stroke:'#748e3e','stroke-width':4,'stroke-dasharray':'7 6',fill:'none'}));
}
async function estimate() {
  const version=++quoteVersion;$('book').disabled=true;draw();
  try {const q=await api('/quote',request());if(version!==quoteVersion)return;$('fare').textContent=money(q.fare);$('distance').textContent=q.distance+' km · straight-line';$('book').disabled=false;message('');}
  catch(e){if(version!==quoteVersion)return;$('fare').textContent='—';$('distance').textContent='Choose a valid route';message(e.message,true);}
}
function text(tag, value) {const el=document.createElement(tag);el.textContent=value;return el;}
async function refresh() {
  const [rides,drivers]=await Promise.all([api('/rides'),api('/drivers')]);
  $('availability').textContent=drivers.filter(d=>d.available).length+' / '+drivers.length+' demo drivers available';
  const list=$('rides');list.replaceChildren();
  if(!rides.length){const p=text('p','No rides yet. Your next journey begins above.');p.className='empty';list.append(p);}
  rides.forEach(ride=>{
    const row=document.createElement('article');row.className='ride';const route=document.createElement('div');
    route.append(text('b',ride.pickup+' → '+ride.dropoff),text('p','#'+ride.id+' · '+ride.rider+' · '+ride.category.toLowerCase()));
    const detail=document.createElement('div');detail.append(text('b',money(ride.fare)),text('p',ride.driver+' · '+ride.distance+' km'));
    const controls=document.createElement('div'),badge=text('span',ride.status.replaceAll('_',' '));badge.className='status';controls.append(badge);
    const actions=document.createElement('div');actions.className='actions';
    const choices=ride.status==='ACCEPTED'?[['start','Start trip'],['cancel','Cancel']]:ride.status==='IN_PROGRESS'?[['complete','Complete trip']]:[];
    choices.forEach(([action,label])=>{const button=text('button',label);button.onclick=async()=>{button.disabled=true;try{await api('/rides/'+ride.id+'/'+action,{});await refresh();message('Ride #'+ride.id+' updated.');}catch(e){message(e.message,true);}finally{button.disabled=false;}};actions.append(button);});controls.append(actions);row.append(route,detail,controls);list.append(row);
  });
}
$('booking').addEventListener('submit',async event=>{event.preventDefault();if(!$('rider').value.trim()){message('Enter your name to book a ride.',true);return;}$('book').disabled=true;try{const ride=await api('/rides',request());await refresh();message('Ride #'+ride.id+' matched with '+ride.driver+'. Manage it in ride activity below.');}catch(e){message(e.message,true);}finally{$('book').disabled=false;}});
['pickup','dropoff'].forEach(id=>$(id).addEventListener('change',estimate));document.querySelectorAll('[name=category]').forEach(el=>el.addEventListener('change',estimate));
$('refresh').onclick=async()=>{try{await refresh();message('Ride activity refreshed.');}catch(e){message(e.message,true);}};
(async()=>{try{places=await api('/places');['pickup','dropoff'].forEach((id,index)=>{const el=$(id);el.replaceChildren();places.forEach((p,i)=>{const option=text('option',p.name);option.value=i;el.append(option);});el.value=index;el.disabled=false;});await estimate();await refresh();}catch(e){message('Unable to connect to the demo server. '+e.message,true);$('availability').textContent='Server unavailable';}})();
