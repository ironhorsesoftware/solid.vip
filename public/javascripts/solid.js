const FOAF = $rdf.Namespace('http://xmlns.com/foaf/0.1/');

// Log the user in and out on click
const popupUri = '/assets/html/solid_popup.html';
$('#login  button').click(() => solid.auth.popupLogin({ popupUri }));
$('#logout button').click(() => solid.auth.logout());

// Update components to match the user's login status
solid.auth.trackSession(session => {
  const loggedIn = !!session;
  $('#login').toggle(!loggedIn);
  $('#logout').toggle(loggedIn);
  if (loggedIn) {
    $('#user').text(session.webId);
    // Use the user's WebID as default profile
    if (!$('#profile').val())
      $('#profile').val(session.webId);
  }
});

$('#update').click(async function loadProfile() {
  // Set up a local data store and associated data fetcher
  const store = $rdf.graph();
  const fetcher = new $rdf.Fetcher(store);
  const updater = new $rdf.UpdateManager(store);

  // Load the person's data into the store
  const person = $('#profile').val();
  await fetcher.load(person);
  var me = store.sym(person);

  // Update the card with the Solid.VIP profile.
  const svpTtl = $('#solidVipProfile').val();
  const svpStore = $rdf.graph();

  $rdf.parse(svpTtl, svpStore, person, 'text/turtle');

  // Save the profile back to the WebID Profile.
  let triples = svpStore.match(undefined, undefined, undefined);
  for (var i = 0; i < triples.length; i++) {
	  console.log("Triple to update: ", i, triples[i]);

	  let ins = $rdf.st(triples[i].subject, triples[i].predicate, triples[i].object, me.doc());
	  let del = store.statementsMatching(triples[i].subject, triples[i].predicate, null, me.doc()) || [];
	  updater.update(del, ins, (uri, ok, message, response) => {
		  console.log("i", i, "uri", uri, "ok", ok, "message", message, "response", response);
	  });
  }
});
